package ai.logsight.backend.logs.domain

import ai.logsight.backend.application.domain.Application

data class LogBatch(
    val application: Application,
    var logs: List<LogsightLog>,
)
