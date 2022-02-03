package ai.logsight.backend.logs.domain.service.command

import ai.logsight.backend.logs.domain.LogFileTypes
import java.util.*

data class LogCommand(
    val userEmail: String,
    val applicationId: UUID,
    val tag: String,
    val logFormat: LogFileTypes,
    val logs: List<String>
)
