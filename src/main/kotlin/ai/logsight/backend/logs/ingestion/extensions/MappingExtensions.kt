package ai.logsight.backend.logs.ingestion.extensions

import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptEntity
import logCount.LogReceipt

fun LogsReceiptEntity.toLogsReceipt() = LogReceipt(
    id = this.id,
    logCount = this.logCount,
    status = this.status,
    batchId = this.batchId,
    processedLogCount = this.processedLogCount
)

fun LogReceipt.toLogsReceiptEntity() = LogsReceiptEntity(
    id = this.id,
    logCount = this.logCount,
    status = this.status,
    batchId = this.batchId,
    processedLogCount = this.processedLogCount
)
