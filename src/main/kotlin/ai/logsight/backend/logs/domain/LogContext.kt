package ai.logsight.backend.logs.domain

import java.util.*

data class LogContext(
    val userId: UUID,
    val appId: UUID,
    val logs: List<String>
)