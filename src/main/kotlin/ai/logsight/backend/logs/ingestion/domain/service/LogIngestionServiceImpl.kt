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
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogStream
import ai.logsight.backend.users.domain.User
import com.antkorwin.xsync.XSync
import kotlinx.serialization.builtins.serializer
import org.springframework.stereotype.Service
import org.thymeleaf.util.ListUtils

@Service
class LogIngestionServiceImpl(
    val logsReceiptStorageService: LogsReceiptStorageService,
    val applicationStorageService: ApplicationStorageService,
    val applicationLifeCycleServiceImpl: ApplicationLifecycleServiceImpl,
    val logStream: LogStream,
    val xSync: XSync<String>
) : LogIngestionService {
    val logger: LoggerImpl = LoggerImpl(LogIngestionServiceImpl::class.java)
    val topicBuilder = TopicBuilder()

    // TODO make configurable
    private var topicPostfix: String = "input"

    private fun handleApplicationAutoCreate(user: User, applicationName: String): Application {
        return try {
            applicationStorageService.findApplicationByUserAndName(user, applicationName)
        } catch (e: ApplicationNotFoundException) {
            applicationLifeCycleServiceImpl.createApplication(CreateApplicationCommand(applicationName, user))
        }
    }
    override fun processLogSingles(logSinglesDTO: LogSinglesDTO): List<LogsReceipt> {
        val groupLogsByApplicationNameAndTag = logSinglesDTO.logs.filter { it.applicationName != null }.groupBy { Pair(it.applicationName, it.tag) }
        val groupLogsByApplicationIdAndTag = logSinglesDTO.logs.filter { it.applicationId != null }.groupBy { Pair(it.applicationId, it.tag) }
        val logBatchDTOs = mutableListOf<LogBatchDTO>()

        groupLogsByApplicationNameAndTag.forEach { groupedByApplicationName ->
            val application = handleApplicationAutoCreate(logSinglesDTO.user, groupedByApplicationName.key.first!!)
            logBatchDTOs.add(
                LogBatchDTO(
                    user = logSinglesDTO.user,
                    application = application,
                    tag = groupedByApplicationName.key.second,
                    logs = groupedByApplicationName.value.map { sendLogMessage ->
                        LogMessage(
                            timestamp = sendLogMessage.timestamp,
                            message = sendLogMessage.message,
                            level = sendLogMessage.level,
                            metadata = sendLogMessage.metadata
                        )
                    },
                    source = logSinglesDTO.source
                )
            )
        }
        groupLogsByApplicationIdAndTag.map { groupedByApplicationId ->
            val application = applicationStorageService.findApplicationById(groupedByApplicationId.key.first!!)
            logBatchDTOs.add(
                LogBatchDTO(
                    user = logSinglesDTO.user,
                    application = application,
                    tag = groupedByApplicationId.key.second,
                    logs = groupedByApplicationId.value.map { sendLogMessage ->
                        LogMessage(
                            timestamp = sendLogMessage.timestamp,
                            message = sendLogMessage.message,
                            level = sendLogMessage.level,
                            metadata = sendLogMessage.metadata
                        )
                    },
                    source = logSinglesDTO.source
                )
            )
        }
        return logBatchDTOs.map { processLogBatch(logBatchDTO = it) }
    }

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
        var sentLogs = 0
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
            sentLogs = logStream.serializeAndSend(topic, logsightLogs)
        }

        return logsReceipt?.let { logsReceiptNotNull ->
            when (sentLogs) {
                logBatchDTO.logs.size -> logsReceiptNotNull
                else -> { // If not all messages were successfully transmitted to logsight core
                    logger.warn(
                        "Not all log messages were transmitted for analysis. " +
                            "Received: ${logBatchDTO.logs.size}. Transmitted: $sentLogs."
                    )
                    logsReceiptStorageService.updateLogsCount(logsReceiptNotNull, sentLogs)
                }
            }
        } ?: throw RuntimeException() // This should not happen
    }
}
