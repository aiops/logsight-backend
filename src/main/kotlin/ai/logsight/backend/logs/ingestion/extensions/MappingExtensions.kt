package ai.logsight.backend.logs.ingestion.extensions

import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogReceiptEntity
import ai.logsight.backend.logs.ingestion.domain.LogReceipt

fun LogReceiptEntity.toLogReceipt() = LogReceipt(
    id = this.id,
    logsCount = this.logsCount,
    status = this.status,
    batchId = this.batchId,
    processedLogCount = this.processedLogCount
)

fun LogReceipt.toLogReceiptEntity() = LogReceiptEntity(
    id = this.id,
    logsCount = this.logsCount,
    status = this.status,
    batchId = this.batchId,
    processedLogCount = this.processedLogCount
)
