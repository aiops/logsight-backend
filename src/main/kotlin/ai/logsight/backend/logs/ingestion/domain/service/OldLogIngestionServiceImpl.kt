/*
package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.application.extensions.isReadyOrException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.logs.domain.LogEvent
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue.LogQueue
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

    override fun processLogEvents(logSinglesDTO: LogEventsDTO): List<LogsReceipt> {
        val logReceiptsApplicationId = logSinglesDTO.logs
            .filter { it.applicationId != null }
            .groupBy { Pair(it.applicationId, it.tag) }
            .map { groupedByApplicationIdAndTag ->
                val application =
                    applicationStorageService.findApplicationById(groupedByApplicationIdAndTag.key.first!!)
                LogBatchDTO(
                    user = logSinglesDTO.user,
                    application = application,
                    tag = groupedByApplicationIdAndTag.key.second,
                    logs = groupedByApplicationIdAndTag.value.map { logMessageDTO ->
                        LogEvent(
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

        val logReceiptsApplicationName = logSinglesDTO.logs
            .filter { it.applicationName != null }
            .groupBy { Pair(it.applicationName, it.tag) }
            .map { groupedByApplicationNameAndTag ->
                val application =
                    handleApplicationAutoCreate(logSinglesDTO.user, groupedByApplicationNameAndTag.key.first!!)
                LogBatchDTO(
                    user = logSinglesDTO.user,
                    application = application,
                    tag = groupedByApplicationNameAndTag.key.second,
                    logs = groupedByApplicationNameAndTag.value.map { logMessageDTO ->
                        LogEvent(
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

        return logReceiptsApplicationId + logReceiptsApplicationName
    }

    override fun processLogBatch(logBatchDTO: LogBatchDTO) {
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
        return logsReceipt ?: throw RuntimeException()
    }
}
*/