package ai.logsight.backend.logs.ingestion.ports.out.sink.serializer

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class LogBatchJsonSerializer(
    val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
) : LogBatchSerializer {
    private val logger = LoggerImpl(LogBatchJsonSerializer::class.java)

    override fun serialize(logBatchDTO: LogBatchDTO): String? {
        val logBatchDTOJsonString = try {
            objectMapper.writeValueAsString(logBatchDTO)
        } catch (ex: JsonProcessingException) {
            logger.warn("Failed to serialize log batch object $logBatchDTO. Reason: $ex")
            null
        }
        return logBatchDTOJsonString
    }

    override fun deserialize(logBatchDTOJsonString: String): LogBatchDTO? {
        val logBatchDTO = try {
            objectMapper.readValue(logBatchDTOJsonString, LogBatchDTO::class.java)
        } catch (ex1: JsonProcessingException) {
            logger.warn(
                "Unable to deserialize string objSerialized $logBatchDTOJsonString into a LogBatchDTO object. " +
                    "Check if JSON is valid. Reason: $ex1"
            )
            null
        } catch (ex2: JsonMappingException) {
            logger.warn(
                "Unable to deserialize string objSerialized $logBatchDTOJsonString into a LogBatchDTO object. " +
                    "Could not map fields. Reason: $ex2"
            )
            null
        }
        return logBatchDTO
    }
}
