package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.logs.ingestion.domain.LogReceipt
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogReceiptCommand
import java.util.*

interface LogReceiptStorageService {
    fun saveLogReceipt(createLogReceiptCommand: CreateLogReceiptCommand): LogReceipt
    fun findLogReceiptById(logReceiptId: UUID): LogReceipt

    fun deleteLogReceipt(logReceiptId: UUID)
}
