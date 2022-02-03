package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.domain.service.command.LogCommand
import ai.logsight.backend.logs.ports.web.requests.SendFileRequest

interface LogsService {
    fun forwardLogs(logCommand: LogCommand)
    fun processFile(logRequest: SendFileRequest, userEmail: String): Application
    fun uploadSampleData(userEmail: String)
}
