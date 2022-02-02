package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.logs.domain.service.command.LogCommand

interface LogsService {
    fun forwardLogs(logCommand: LogCommand)
}
