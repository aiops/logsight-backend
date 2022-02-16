package ai.logsight.backend.common.utils

import ai.logsight.backend.common.logging.LoggerImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class TopicJsonSerializer(
    val objectMapper: ObjectMapper = ObjectMapper()
) {
    fun serialize(topic: String, obj: Any): String = "$topic ${objectMapper.writeValueAsString(obj)}"

    fun <T> deserialize(obj_serialized: String, cls: Class<T>): T =
        objectMapper.readValue(obj_serialized.split(" ", limit = 2)[1], cls)
}
