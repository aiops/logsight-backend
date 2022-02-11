package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.logs.domain.LogFormats
import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO
import ai.logsight.backend.logs.domain.service.helpers.TopicBuilder
import ai.logsight.backend.logs.exceptions.LogFileIOException
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ports.out.stream.LogStream
import ai.logsight.backend.users.domain.User
import com.antkorwin.xsync.XSync
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

enum class LogDataSources(val source: String) {
    REST_BATCH("restBatch"), FILE("file"), SAMPLE("sample")
}

@Service
class LogsServiceImpl(
    val logsReceiptStorageService: LogsReceiptStorageService,
    val applicationLifecycleService: ApplicationLifecycleService,
    val logStream: LogStream,
    val topicBuilder: TopicBuilder,
    val xSync: XSync<String>
) : LogsService {
    val logger: Logger = LoggerFactory.getLogger(LogsServiceImpl::class.java)

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    object SampleLogConstants {
        val SAMPLE_LOGS_APP_NAMES = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")
        const val SAMPLE_LOG_DIR = "sample_data"
        const val SAMPLE_TAG = "default"
    }

    override fun processLogBatch(logBatchDTO: LogBatchDTO): LogsReceipt {
        verifyApplicationReadyState(logBatchDTO.application)
        return processLogs(
            logBatchDTO.user, logBatchDTO.application, logBatchDTO.logFormat.toString(),
            logBatchDTO.tag, LogDataSources.REST_BATCH.source, logBatchDTO.logs
        )
    }

    @Throws(LogFileIOException::class)
    override fun processLogFile(logFileDTO: LogFileDTO): LogsReceipt {
        verifyApplicationReadyState(logFileDTO.application)
        val fileContent = readFileContent(logFileDTO.file.name, logFileDTO.file.inputStream)
        val logMessages = convertFileContentToStringList(fileContent)
        return processLogs(
            logFileDTO.user, logFileDTO.application, logFileDTO.logFormats.toString(),
            logFileDTO.tag, LogDataSources.FILE.source, logMessages
        )
    }

    private fun readFileContent(fileName: String, inputStream: InputStream): String =
        try {
            inputStream.readBytes().toString(Charsets.UTF_8)
        } catch (e: Exception) {
            throw LogFileIOException("Error while reading file content of file $fileName. Reason: ${e.message}")
        }

    // TODO (This must be tested)
    private fun convertFileContentToStringList(fileContent: String): List<String> {
        val logMessages = mutableListOf<String>()
        val stringBuilder = StringBuilder()
        // Filters empty lines and appends multiline logs to one line based on heuristic that
        // multiline logs start with spaces
        fileContent.lines().filter { it.isNotEmpty() }.forEach {
            stringBuilder.append(it.trim().plus(" "))
            if (!it.first().isWhitespace()) {
                logMessages.add(stringBuilder.toString().trim())
                stringBuilder.clear()
            }
        }
        if (stringBuilder.isNotEmpty())
            logMessages.add(stringBuilder.toString().trim())
        return logMessages
    }

    override fun processLogSample(logSampleDTO: LogSampleDTO): LogsReceipt {
        val logsReceipts = SampleLogConstants.SAMPLE_LOGS_APP_NAMES.map { appName ->
            // TODO Recreation might be required if time mapping of sample logs is implemented
            val app = applicationLifecycleService.createApplication(
                CreateApplicationCommand(appName, logSampleDTO.user)
            )
            // App lifecycle service might alter the name of the app. Therefore, the original appName is used as
            // argument here.
            val filePath = Paths.get(resourcesPath, SampleLogConstants.SAMPLE_LOG_DIR, appName)
            val fileContent = try {
                readFileContent(appName, File(filePath.toUri()).inputStream())
            } catch (e: Exception) {
                throw LogFileIOException("Error while reading sample log file $filePath. Reason: ${e.message}")
            }
            val logMessages = convertFileContentToStringList(fileContent)
            processLogs(
                logSampleDTO.user, app, LogFormats.UNKNOWN_FORMAT.toString(),
                SampleLogConstants.SAMPLE_TAG, LogDataSources.SAMPLE.source, logMessages
            )
        }
        return logsReceipts.last()
    }

    fun processLogs(
        user: User,
        app: Application,
        format: String,
        tag: String,
        source: String,
        logMessages: Collection<String>
    ): LogsReceipt {
        val createLogsReceiptCommand = CreateLogsReceiptCommand(
            logsCount = logMessages.size,
            source = source,
            application = app
        )
        val topic = topicBuilder.buildTopic(user.key, app.name)

        // Order and transmission to logsight core are synchronized via mutex
        var logsReceipt: LogsReceipt? = null
        var sentLogs = 0
        xSync.execute("logs-stream") { // TODO define mutex constants somewhere else
            logsReceipt = logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand)
            val logs = logMessages.map { message ->
                Log(app.name, app.id.toString(), user.key, format, tag, logsReceipt!!.orderCounter, message)
            }
            sentLogs = logStream.serializeAndSend(topic, logs)
        }

        return logsReceipt?.let { logsReceiptNotNull ->
            when (sentLogs) {
                logMessages.size -> logsReceiptNotNull
                else -> { // If not all messages were successfully transmitted to logsight core
                    logger.warn(
                        "Not all log messages were transmitted for analysis. " +
                            "Received: ${logMessages.size}. Transmitted: $sentLogs."
                    )
                    logsReceiptStorageService.updateLogsCount(logsReceiptNotNull, sentLogs)
                }
            }
        } ?: throw RuntimeException() // This should happen
    }

    private fun verifyApplicationReadyState(application: Application) {
        if (application.status != ApplicationStatus.READY) {
            throw ApplicationStatusException(
                "To receive logs, tha application ${application.name} must be in state " +
                    "${ApplicationStatus.READY.name} but is currently in state ${application.status.name}."
            )
        }
    }
}
