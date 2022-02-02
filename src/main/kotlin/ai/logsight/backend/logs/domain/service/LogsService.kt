package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.logs.domain.LogDTO

interface LogsService {
    fun forwardLogs(logDTO: LogDTO)
}
