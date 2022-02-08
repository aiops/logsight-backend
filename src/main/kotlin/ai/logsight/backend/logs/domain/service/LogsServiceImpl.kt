package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.exceptions.LogFileReadingException
import ai.logsight.backend.exceptions.LogsReceiptException
import ai.logsight.backend.logs.domain.LogFormat
import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO
import ai.logsight.backend.logs.domain.service.helpers.TopicBuilder
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ports.out.stream.LogStream
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.antkorwin.xsync.XSync
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

@Service
class LogsServiceImpl(
    val logsReceiptStorageService: LogsReceiptStorageService,
    val userStorageService: UserStorageService,
    val applicationLifecycleService: ApplicationLifecycleService,
    val applicationStorageService: ApplicationStorageService,
    val logStream: LogStream,
    val topicBuilder: TopicBuilder
) : LogsService {
    val logger: Logger = LoggerFactory.getLogger(LogsServiceImpl::class.java)

    @Autowired
    private val xSync: XSync<Application>? = null

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    object SampleLogConstants {
        val SAMPLE_LOGS_APP_NAMES = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")
        const val SAMPLE_LOG_DIR = "sample_data"
    }

    override fun processLogBatch(logBatchDTO: LogBatchDTO): LogsReceipt {
        val app = applicationStorageService.findApplicationById(logBatchDTO.applicationId)
        val user = userStorageService.findUserByEmail(logBatchDTO.userEmail)
        return processLogs(user, app, logBatchDTO.logFormat.toString(), logBatchDTO.tag, "restBatch", logBatchDTO.logs)
    }

    @Throws(LogFileReadingException::class)
    override fun processLogFile(logFileDTO: LogFileDTO): LogsReceipt {
        val user = userStorageService.findUserByEmail(logFileDTO.userEmail)
        // Auto-create app if it is not present
        val app = applicationLifecycleService.createApplication(
            CreateApplicationCommand(logFileDTO.applicationName, user)
        )
        val fileContent = readFileContent(logFileDTO.file.name, logFileDTO.file.inputStream)
        val logMessages = convertFileContentToStringList(fileContent)
        return processLogs(user, app, logFileDTO.logFormat.toString(), logFileDTO.tag, "file", logMessages)
    }

    private fun readFileContent(fileName: String, inputStream: InputStream): String =
        try {
            inputStream.readBytes().toString(Charsets.UTF_8)
        } catch (e: Exception) {
            throw LogFileReadingException("Error while reading file content of file $fileName. Reason: ${e.message}")
        }

    // TODO (This must be extensively tested)
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

    override fun processLogSample(logSampleDTO: LogSampleDTO) {
        val user = userStorageService.findUserByEmail(logSampleDTO.userEmail)
        SampleLogConstants.SAMPLE_LOGS_APP_NAMES.forEach { appName ->
            // TODO Recreation might be required if time mapping of sample logs is implemented
            val app = applicationLifecycleService.createApplication(
                CreateApplicationCommand(appName, user)
            )
            // App lifecycle service might alter the name of the app. Therefore, the original appName is used as
            // argument here.
            val filePath = Paths.get(resourcesPath, SampleLogConstants.SAMPLE_LOG_DIR, appName)
            // TODO: exception handling
            val fileContent = readFileContent(appName, File(filePath.toUri()).inputStream())
            val logMessages = convertFileContentToStringList(fileContent)
            processLogs(user, app, LogFormat.UNKNOWN_FORMAT.toString(), "default", "file", logMessages)
        }
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
            logsCount = logMessages.size.toLong(),
            source = source,
            application = app
        )
        val topic = topicBuilder.buildTopic(user.key, app.name)

        var logsReceipt: LogsReceipt? = null
        xSync!!.execute(app) {
            logsReceipt = logsReceiptStorageService.saveLogReceipt(createLogsReceiptCommand)
            val logs = logMessages.map { message ->
                Log(app.name, app.id.toString(), user.key, format, tag, logsReceipt!!.orderCounter, message)
            }
            logStream.serializeAndSend(topic, logs)
        }
        return logsReceipt ?: throw LogsReceiptException()
    }
}
