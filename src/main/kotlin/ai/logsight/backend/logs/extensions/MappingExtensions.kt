package ai.logsight.backend.logs.extensions

import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptEntity

fun LogsReceiptEntity.toLogsReceipt() = LogsReceipt(
    orderCounter = this.orderCounter,
    logsCount = this.logsCount,
    source = this.source,
    application = this.application.toApplication(),
)
