package ai.logsight.backend.logs.ports.out.stream.adapters.zeromq

import ai.logsight.backend.logs.domain.service.helpers.TopicBuilder
import ai.logsight.backend.logs.ports.out.stream.LogStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.zeromq.ZMQ

@Component
class ZeroMQPubStream(
    val topicBuilder: TopicBuilder
) : LogStream {
    @Autowired
    private lateinit var zeroMqPubSocket: ZMQ.Socket

    override fun send(topic: String, logs: List<Log>) {
        val objectMapper = ObjectMapper()
        logs.forEach { log ->
            zeroMqPubSocket.send("$topic ${objectMapper.writeValueAsString(log)}")
        }
    }
}
