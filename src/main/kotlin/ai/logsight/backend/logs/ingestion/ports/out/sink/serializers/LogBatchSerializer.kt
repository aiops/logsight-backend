package ai.logsight.backend.logs.ingestion.ports.out.sink.serializers

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO

interface LogBatchSerializer {
    fun serialize(obj: LogBatchDTO): String
    fun deserialize(obj_serialized: String): LogBatchDTO
}
