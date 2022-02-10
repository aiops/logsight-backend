package ai.logsight.backend.logs.ports.out.persistence

import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand

interface LogsReceiptStorageService {
    fun saveLogsReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogsReceipt
    fun findLogsReceiptById(logReceiptId: Long): LogsReceipt
    fun updateLogsCount(logsReceipt: LogsReceipt, logsCount: Int): LogsReceipt
}