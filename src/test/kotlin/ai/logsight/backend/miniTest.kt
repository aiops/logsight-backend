package ai.logsight.backend

import ai.logsight.backend.logs.domain.service.dto.Log
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.ZeroMQPubStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class miniTest {

    @Autowired
    lateinit var logSend: ZeroMQPubStream

    @Test
    fun `should print`() {
        // given
        val log = Log("app_name", "pk", "log_type", "tag", 0, "msg")
        val topic = "test"

        logSend.serializeAndSend(topic, listOf(log))
        // when
        println(ObjectMapper().writeValueAsString(log))

        // then
    }
}
