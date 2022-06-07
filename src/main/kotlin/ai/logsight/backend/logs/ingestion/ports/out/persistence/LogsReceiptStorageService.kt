package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import logCount.LogReceipt
import java.util.*

interface LogsReceiptStorageService {
    fun saveLogReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogReceipt
    fun findLogReceiptById(logReceiptId: UUID): LogReceipt

    fun updateLogReceiptStatus(logReceiptId: UUID, status: LogBatchStatus): LogReceipt
    fun deleteLogReceipt(logReceiptId: UUID)
}
