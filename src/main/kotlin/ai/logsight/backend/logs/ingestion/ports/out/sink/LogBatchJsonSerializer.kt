package ai.logsight.backend.logs.ingestion.ports.out.sink

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class LogBatchJsonSerializer(
    val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
) : LogBatchSerializer {
    override fun serialize(obj: LogBatchDTO): String = objectMapper.writeValueAsString(obj)

    override fun deserialize(obj_serialized: String): LogBatchDTO =
        objectMapper.readValue(obj_serialized, LogBatchDTO::class.java)
}
