package ai.logsight.backend.flush.extensions

import ai.logsight.backend.flush.domain.Flush
import ai.logsight.backend.flush.ports.persistence.FlushEntity
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceipt
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceiptEntity

fun FlushEntity.toFlush() = Flush(
    id = this.id,
    status = this.status,
    logsReceipt = this.logsReceipt.toLogsReceipt()
)

fun Flush.toFlushEntity() = FlushEntity(
    id = this.id,
    status = this.status,
    logsReceipt = this.logsReceipt.toLogsReceiptEntity()
)
