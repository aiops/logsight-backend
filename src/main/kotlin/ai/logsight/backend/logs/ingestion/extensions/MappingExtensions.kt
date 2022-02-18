package ai.logsight.backend.logs.ingestion.extensions

import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptEntity

fun LogsReceiptEntity.toLogsReceipt() = LogsReceipt(
    id = this.id,
    orderNum = this.orderNum,
    logsCount = this.logsCount,
    source = this.source,
    application = this.application.toApplication()
)

fun LogsReceipt.toLogsReceiptEntity() = LogsReceiptEntity(
    id = this.id,
    orderNum = this.orderNum,
    logsCount = this.logsCount,
    source = this.source,
    application = this.application.toApplicationEntity()
)
