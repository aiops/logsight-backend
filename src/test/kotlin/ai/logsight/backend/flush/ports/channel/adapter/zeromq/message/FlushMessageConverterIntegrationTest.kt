package ai.logsight.backend.flush.ports.channel.adapter.zeromq.message

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.test.assertEquals

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
internal class FlushMessageConverterIntegrationTest {

    @Autowired
    lateinit var flushMessageConverter: FlushMessageConverter

    companion object {
        const val id = "cbc8d7d3-307b-4b27-ba43-37f15b6c1d9c"
        const val orderNum = 1
        const val logsCount = 1
        const val description = "Nice"
        const val status = 200

        val test_input = """
        {
            "id": "$id",
            "orderNum": "$orderNum",
            "logsCount": "$logsCount",
            "currentLogsCount": "$logsCount",
            "description": "$description",
            "status": "$status"
        }
        """.trimIndent()
    }

    @Nested
    @DisplayName("Test deserialization of flush RPC response")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeserializeFlushRPC {

        @Test
        fun `should deserialize rpc response`() {
            // given
            val serializedMessage = "some_topic_name $test_input"
            // when
            val message = flushMessageConverter.toMessage(serializedMessage.toByteArray(), null)
            val flushMessage = message?.payload as FlushMessage

            // then
            assertEquals(flushMessage.id, UUID.fromString(id))
            assertEquals(flushMessage.orderNum, orderNum)
            assertEquals(flushMessage.logsCount, logsCount)
            assertEquals(flushMessage.currentLogsCount, logsCount)
            assertEquals(flushMessage.description, description)
            assertEquals(flushMessage.status.value(), status)
        }
    }
}
