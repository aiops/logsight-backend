package ai.logsight.backend.common.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class TopicJsonSerializer(
    val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
) {
    fun serialize(obj: Any): String = objectMapper.writeValueAsString(obj)

    fun <T> deserialize(obj_serialized: String, cls: Class<T>): T =
        objectMapper.readValue(obj_serialized.split(" ", limit = 2)[1], cls)
}
