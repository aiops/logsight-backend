package ai.logsight.backend.logs.domain.service.dto

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.domain.LogFormat
import ai.logsight.backend.users.domain.User

data class LogBatchDTO(
    val user: User,
    val application: Application,
    val tag: String,
    val logFormat: LogFormat,
    val logs: List<String>
)
