package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.logs.ingestion.domain.LogReceipt
import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogReceiptCommand
import ai.logsight.backend.logs.ingestion.exceptions.LogReceiptNotFoundException
import ai.logsight.backend.logs.ingestion.extensions.toLogReceipt
import org.springframework.stereotype.Service
import java.util.*

@Service
class LogReceiptStorageServiceImpl(
    private val logReceiptRepository: LogReceiptRepository,
) : LogReceiptStorageService {
    override fun saveLogReceipt(createLogReceiptCommand: CreateLogReceiptCommand): LogReceipt {
        val logReceiptEntity = LogReceiptEntity(
            logsCount = createLogReceiptCommand.logsCount,
            batchId = createLogReceiptCommand.batchId,
            status = LogBatchStatus.PROCESSING
        )
        return logReceiptRepository.save(logReceiptEntity).toLogReceipt()
    }

    override fun findLogReceiptById(logReceiptId: UUID): LogReceipt {
        return findLogReceiptByIdPrivate(logReceiptId).toLogReceipt()
    }

    private fun findLogReceiptByIdPrivate(logReceiptId: UUID): LogReceiptEntity {
        return logReceiptRepository.findById(logReceiptId)
            .orElseThrow { LogReceiptNotFoundException("LogRequest receipt with id $logReceiptId is not found") }
    }

    override fun deleteLogReceipt(logReceiptId: UUID) {
        logReceiptRepository.deleteById(logReceiptId)
    }
}
