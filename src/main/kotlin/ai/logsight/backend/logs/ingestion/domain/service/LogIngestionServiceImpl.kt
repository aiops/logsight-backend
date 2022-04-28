package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.extensions.isReadyOrException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogSinglesDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogQueue
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogStream
import ai.logsight.backend.users.domain.User
import com.antkorwin.xsync.XSync
import org.springframework.stereotype.Service

@Service
class LogIngestionServiceImpl(
    val logsReceiptStorageService: LogsReceiptStorageService,
    val applicationStorageService: ApplicationStorageService,
    val applicationLifeCycleServiceImpl: ApplicationLifecycleServiceImpl,
    val logQueue: LogQueue,
    val xSync: XSync<String>
) : LogIngestionService {
    val logger: LoggerImpl = LoggerImpl(LogIngestionServiceImpl::class.java)
    val topicBuilder = TopicBuilder()

    // TODO make configurable
    private var topicPostfix: String = "input"

    // Pool for random application name
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun generateRandomApplicationName(): String {
        return (1..10)
            .map { _ -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun validateApplicationName(applicationName: String): String {
        var applicationNameValidated = applicationName.lowercase().replace(("[^\\w_-]").toRegex(), "")
        if (applicationNameValidated.isEmpty()) {
            applicationNameValidated = generateRandomApplicationName()
        }
        return applicationNameValidated
    }

    private fun handleApplicationAutoCreate(user: User, applicationName: String): Application {
        val applicationNameValidated = validateApplicationName(applicationName)
        return try {
            applicationStorageService.findApplicationByUserAndName(user, applicationNameValidated)
        } catch (e: ApplicationNotFoundException) {
            applicationLifeCycleServiceImpl.createApplication(CreateApplicationCommand(applicationNameValidated, user))
        }
    }

    override fun processLogSingles(logSinglesDTO: LogSinglesDTO): List<LogsReceipt> = logSinglesDTO.logs
        .map { log ->
            // Get all logs where application name is set and application ID is not set
            // These applications need an auto-creation handling (see handleApplicationAutoCreate)
            val application = if (log.applicationId == null && log.applicationName != null) {
                handleApplicationAutoCreate(logSinglesDTO.user, log.applicationName)
                // Get all logs where the application ID is set. These apps are assumed to be already created
            } else {
                applicationStorageService.findApplicationById(log.applicationId!!)
            }
            log.toLogMessageDTO(application)
        }
        .groupBy { Pair(it.application, it.tag) }
        .map { groupedByApplicationIdAndTag ->
            LogBatchDTO(
                user = logSinglesDTO.user,
                application = groupedByApplicationIdAndTag.key.first,
                tag = groupedByApplicationIdAndTag.key.second,
                logs = groupedByApplicationIdAndTag.value.map { logMessageDTO ->
                    LogMessage(
                        timestamp = logMessageDTO.timestamp,
                        message = logMessageDTO.message,
                        level = logMessageDTO.level,
                        metadata = logMessageDTO.metadata
                    )
                },
                source = logSinglesDTO.source
            )
        }
        .map { processLogBatch(logBatchDTO = it) }

    override fun processLogBatch(logBatchDTO: LogBatchDTO): LogsReceipt {
        logBatchDTO.application.isReadyOrException()

        val createLogsReceiptCommand = CreateLogsReceiptCommand(
            logsCount = logBatchDTO.logs.size,
            source = logBatchDTO.source.name,
            application = logBatchDTO.application
        )
        val topic = topicBuilder.buildTopic(listOf(logBatchDTO.user.key, logBatchDTO.application.name, topicPostfix))

        // Order and transmission to logsight core are synchronized via mutex
        var logsReceipt: LogsReceipt? = null
        xSync.execute("logs-stream") { // TODO define mutex constants somewhere else
            logsReceipt = logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand)
            val logsightLogs = logBatchDTO.logs.map { message ->
                LogsightLog(
                    logBatchDTO.application.name,
                    logBatchDTO.application.id.toString(),
                    logBatchDTO.user.key,
                    logBatchDTO.source,
                    logBatchDTO.tag,
                    logsReceipt!!.orderNum,
                    message
                )
            }
            logQueue.addAll(topic, logsightLogs)
        }
        return logsReceipt?: throw RuntimeException()
    }
}
