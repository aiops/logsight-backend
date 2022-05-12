package ai.logsight.backend.logs.ingestion.ports.out.sink.serializers

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class TopicBatchSerializer(
    val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
) : LogBatchSerializer {
    fun serialize(topic: String, obj: Any): String = "$topic ${objectMapper.writeValueAsString(obj)}"
    override fun serialize(obj: LogBatchDTO): String {
        return objectMapper.writeValueAsString(obj)
    }

    override fun deserialize(obj_serialized: String): LogBatchDTO =
        objectMapper.readValue(obj_serialized.split(" ", limit = 2)[1], LogBatchDTO::class.java)
}
