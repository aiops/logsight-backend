package ai.logsight.backend.logs.domain.service.command

import ai.logsight.backend.logs.domain.LogFormat
import java.util.*

data class LogCommand(
    val userEmail: String,
    val applicationId: UUID,
    val tag: String,
    val logFormat: LogFormat,
    val logs: List<String>
)
