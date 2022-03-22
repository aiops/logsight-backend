package ai.logsight.backend.flush.ports.channel.adapter.zeromq.message

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.http.HttpStatus
import java.util.*

data class FlushMessage(
    val id: UUID,
    val orderNum: Int,
    val logsCount: Int,
    val currentLogsCount: Int,
    val description: String,
    @JsonDeserialize(using = HttpStatusConversions.Deserializer::class)
    val status: HttpStatus
)

object HttpStatusConversions {
    object Deserializer : JsonDeserializer<HttpStatus>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): HttpStatus {
            val node = p.readValueAsTree<JsonNode>()
            val httpStatusValue = node.asInt()
            return HttpStatus.valueOf(httpStatusValue)
        }
    }
}





