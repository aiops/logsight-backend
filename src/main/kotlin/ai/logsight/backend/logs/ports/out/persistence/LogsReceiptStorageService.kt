package ai.logsight.backend.logs.ports.out.persistence

import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand
import java.util.*

interface LogsReceiptStorageService {
    fun saveLogsReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogsReceipt
    fun findLogsReceiptById(logReceiptId: UUID): LogsReceipt
    fun updateLogsCount(logsReceipt: LogsReceipt, logsCount: Int): LogsReceipt
}