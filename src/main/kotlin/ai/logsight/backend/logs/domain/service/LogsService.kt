package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO

interface LogsService {
    fun forwardLogs(logBatchDTO: LogBatchDTO)
    fun processFile(logFileDTO: LogFileDTO)
    fun uploadSampleData(logSampleDTO: LogSampleDTO)
}
