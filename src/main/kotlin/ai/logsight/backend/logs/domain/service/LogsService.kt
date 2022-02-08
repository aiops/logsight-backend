package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO

interface LogsService {
    fun processLogBatch(logBatchDTO: LogBatchDTO): LogsReceipt
    fun processLogFile(logFileDTO: LogFileDTO): LogsReceipt
    fun processLogSample(logSampleDTO: LogSampleDTO)
}
