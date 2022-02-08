package ai.logsight.backend.logs.ports.out.persistence

import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand

interface LogsReceiptStorageService {
    fun saveLogReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogsReceipt
    fun findLogReceiptById(logReceiptId: Long): LogsReceipt
}