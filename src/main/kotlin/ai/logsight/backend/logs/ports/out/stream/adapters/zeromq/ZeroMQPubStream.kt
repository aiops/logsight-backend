package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.helpers.TopicBuilder
import ai.logsight.backend.logs.ports.out.stream.LogStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class ZeroMQPubStream(
    @Qualifier("pub") val zeroMqPubSocket: ZMQ.Socket
) : LogStream {
    val topicBuilder = TopicBuilder()

    override fun sendBatch(batch: LogBatchDTO) {
        val topic = topicBuilder.buildTopic(batch.userKey, batch.applicationName)
        val objectMapper = ObjectMapper()
        batch.logs.forEach { log ->
            zeroMqPubSocket.send("$topic ${objectMapper.writeValueAsString(log)}")
        }
    }
}
