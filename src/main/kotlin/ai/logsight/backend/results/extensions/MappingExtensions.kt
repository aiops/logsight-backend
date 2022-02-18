package ai.logsight.backend.results.extensions

import ai.logsight.backend.logs.ingestion.extensions.toLogsReceipt
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceiptEntity
import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.ports.persistence.ResultInitEntity

fun ResultInitEntity.toResultInit() = ResultInit(
    id = this.id,
    status = this.status,
    logsReceipt = this.logsReceipt.toLogsReceipt()
)

fun ResultInit.toResultInitEntity() = ResultInitEntity(
    id = this.id,
    status = this.status,
    logsReceipt = this.logsReceipt.toLogsReceiptEntity()
)
