package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.logs.domain.LogContext

interface LogsService {
    fun forwardLogs(logContext: LogContext): Int
}