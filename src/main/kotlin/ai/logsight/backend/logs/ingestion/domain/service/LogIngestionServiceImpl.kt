package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.extensions.toLogsightLog
import ai.logsight.backend.logs.ingestion.domain.LogReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogListDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.exceptions.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import org.springframework.stereotype.Service

@Service
class LogIngestionServiceImpl(
    private val logReceiptStorageService: LogReceiptStorageService,
    private val logSink: LogSink,
) : LogIngestionService {

    override fun processLogBatch(logBatch: LogBatch): LogReceipt {
        val createLogReceiptCommand = CreateLogReceiptCommand(
            logsCount = logBatch.logs.size,
            batchId = logBatch.id
        )
        val receipt = logReceiptStorageService.saveLogReceipt(createLogReceiptCommand)
        try {
            logSink.sendLogBatch(logBatch.toLogBatchDTO())
        } catch (e: LogSinkException) {
            logReceiptStorageService.deleteLogReceipt(receipt.id)
            throw LogSinkException(e.message)
        }
        return receipt
    }

    override fun processLogList(logList: LogListDTO): LogReceipt {
        val logBatch = logBatchFromLogList(logList)
        return processLogBatch(logBatch)
    }

    override fun processLogEvents(logEventsDTO: LogEventsDTO): LogReceipt {
        val logBatch = logBatchFromEvents(logEventsDTO)
        return processLogBatch(logBatch)
    }

    private fun logBatchFromEvents(logEventsDTO: LogEventsDTO) = LogBatch(
        index = logEventsDTO.index,
        logs = logEventsDTO.logs.map { log ->
            log.toLogsightLog()
        }
    )

    private fun logBatchFromLogList(logList: LogListDTO) = LogBatch(
        index = logList.index,
        logs = logList.logs.map {
            LogsightLog(
                timestamp = it.timestamp,
                message = it.message,
                level = it.level,
                tags = logList.tags
            )
        }
    )
}
