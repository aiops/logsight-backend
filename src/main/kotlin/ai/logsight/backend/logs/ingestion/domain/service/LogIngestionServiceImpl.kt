package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.application.extensions.isReadyOrException
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogStream
import com.antkorwin.xsync.XSync
import org.springframework.stereotype.Service

@Service
class LogIngestionServiceImpl(
    val logsReceiptStorageService: LogsReceiptStorageService,
    val logStream: LogStream,
    val xSync: XSync<String>
) : LogIngestionService {
    val logger: LoggerImpl = LoggerImpl(LogIngestionServiceImpl::class.java)
    val topicBuilder = TopicBuilder()

    // TODO make configurable
    private var topicPostfix: String = "input"

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
