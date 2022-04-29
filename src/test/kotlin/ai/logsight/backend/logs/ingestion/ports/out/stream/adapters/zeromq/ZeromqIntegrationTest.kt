package ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq

import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq.config.LogStreamZeroMqConfigProperties
import com.sun.mail.iap.ConnectionException
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@DirtiesContext
class ZeromqIntegrationTest {

    @Autowired
    lateinit var logStreamZeroMq: LogStreamZeroMq

    @Autowired
    lateinit var zeroMqConf: LogStreamZeroMqConfigProperties

    @Autowired
    lateinit var topicJsonSerializer: TopicJsonSerializer

    companion object {
        private val topicBuilder = TopicBuilder()

        private val source = LogDataSources.REST_BATCH
        private const val tag = "default"
        private const val app_name = "test_app1"
        private val app_id = UUID.randomUUID()
        private const val private_key = "some_key"
        private const val orderCounter = 1L
        private const val message = "Hello World!"
        private val timestamp = DateTime.now().toString()
        private const val level = "INFO"
        private val logMessage = LogMessage(timestamp, message, level)
        private val log = LogsightLog(app_name, app_id.toString(), private_key, source, tag, orderCounter, logMessage)
    }

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        private fun getZeroMqTestSocket(topic: String): ZMQ.Socket {
            val ctx = ZContext()
            val zeroMQSocket = ctx.createSocket(SocketType.SUB)
            val addr = "${zeroMqConf.protocol}://0.0.0.0:${zeroMqConf.port}"
            val status = zeroMQSocket.connect(addr)
            if (!status) throw ConnectionException("Test ZeroMQ SUB is not able to connect socket to $addr")
            zeroMQSocket.subscribe(topic)
            Thread.sleep(5)
            return zeroMQSocket
        }

        @Test
        fun `should send log to zeromq success`() {
            // given
            val topic = topicBuilder.buildTopic(listOf(private_key, app_name))

            // when
            val success = logStreamZeroMq.serializeAndSend(topic, log)

            // then
            Assertions.assertTrue(success)
        }

        @Test
        fun `test transmission via zeromq`() {
            // given
            val topic = topicBuilder.buildTopic(listOf(private_key, app_name))
            val numLogs = 1000
            val logs = List(numLogs) { log }
            val zeroMQSocket = getZeroMqTestSocket(topic)

            // when
            val successes = logs.map {
                logStreamZeroMq.serializeAndSend(topic, it)
            }

            // then
            Assertions.assertEquals(successes.size, numLogs)
            Assertions.assertTrue(successes.all { it })
            verifyZeroMqTransmission(zeroMQSocket, logs)
        }

        private fun verifyZeroMqTransmission(zeroMQSocket: ZMQ.Socket, sentLogs: List<LogsightLog>) {
            val serializedLogs = List(sentLogs.size) { String(zeroMQSocket.recv()) }
            val receivedLogs =
                serializedLogs.map { topicJsonSerializer.deserialize(it, LogsightLog::class.java) }

            // num. sent logs = num. received logs
            Assertions.assertEquals(receivedLogs.size, sentLogs.size)
            Assertions.assertEquals(sentLogs, receivedLogs)
        }
    }
}
