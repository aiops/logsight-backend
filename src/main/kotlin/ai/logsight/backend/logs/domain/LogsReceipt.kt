package ai.logsight.backend.logs.domain

import ai.logsight.backend.application.domain.Application
import java.util.*

data class LogsReceipt(
    val id: UUID,
    val orderNum: Long,
    val logsCount: Int,
    val source: String,
    val application: Application
)
