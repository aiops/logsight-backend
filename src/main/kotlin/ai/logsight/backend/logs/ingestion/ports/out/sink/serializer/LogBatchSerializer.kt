package ai.logsight.backend.logs.ingestion.ports.out.sink.serializer

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO

interface LogBatchSerializer {
    fun serialize(logBatchDTO: LogBatchDTO): String?
    fun deserialize(logBatchDTOJsonString: String): LogBatchDTO?
}
