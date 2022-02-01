package ai.logsight.backend.logs.domain

import java.util.*

data class LogDTO(
    val email: String,
    val appName: UUID,
    val logs: List<String>
)
