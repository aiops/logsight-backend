package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.exceptions.LogsReceiptNotFoundException
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceipt
import logCount.LogReceipt
import org.springframework.stereotype.Service
import java.util.*

@Service
class LogsReceiptStorageServiceImpl(
    private val logsReceiptRepository: LogsReceiptRepository,
) : LogsReceiptStorageService {
    override fun saveLogReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogReceipt {
        val logsReceiptEntity = LogsReceiptEntity(
            logCount = createLogsReceiptCommand.logsCount,
            batchId = createLogsReceiptCommand.batchId,
            status = LogBatchStatus.PROCESSING
        )
        return logsReceiptRepository.save(logsReceiptEntity).toLogsReceipt()
    }

    override fun findLogReceiptById(logReceiptId: UUID): LogReceipt {
        return findLogsReceiptByIdPrivate(logReceiptId).toLogsReceipt()
    }

    private fun findLogsReceiptByIdPrivate(logsReceiptId: UUID): LogsReceiptEntity {
        return logsReceiptRepository.findById(logsReceiptId)
            .orElseThrow { LogsReceiptNotFoundException("LogRequest receipt with id $logsReceiptId is not found") }
    }

    override fun updateLogReceiptStatus(logReceiptId: UUID, status: LogBatchStatus): LogReceipt {
        val logReceiptEntity = findLogsReceiptByIdPrivate(logReceiptId)
        logReceiptEntity.status = status
        logsReceiptRepository.save(logReceiptEntity)
        return logReceiptEntity.toLogsReceipt()
    }

    override fun deleteLogReceipt(logReceiptId: UUID) {
        logsReceiptRepository.deleteById(logReceiptId)
    }
}
