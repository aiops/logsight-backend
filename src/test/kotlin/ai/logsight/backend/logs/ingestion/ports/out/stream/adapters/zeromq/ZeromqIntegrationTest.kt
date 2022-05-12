package ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq

import ai.logsight.backend.TestInputConfig.logBatch
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.ZeroMqSink
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.config.LogSinkZeroMqConfigProperties
import ai.logsight.backend.logs.ingestion.ports.out.sink.serializers.TopicBatchSerializer
import com.sun.mail.iap.ConnectionException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@DirtiesContext
class ZeromqIntegrationTest {

    @Autowired
    lateinit var zeroMqSink: ZeroMqSink

    @Autowired
    lateinit var zeroMqConf: LogSinkZeroMqConfigProperties

    @Autowired
    lateinit var topicBatchSerializer: TopicBatchSerializer

    companion object {
        private val topicBuilder = TopicBuilder()
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

            // when
            zeroMqSink.sendBatch(logBatch.toLogBatchDTO())

            // then
        }

        @Test
        fun `test transmission via zeromq`() {
            // given
            val numLogs = 1000
            val logs = List(numLogs) { logBatch.toLogBatchDTO() }
            val zeroMQSocket = getZeroMqTestSocket("test")

            // when
            val successes = logs.map {
                zeroMqSink.sendBatch(it)
            }

            // then
            Assertions.assertEquals(successes.size, numLogs)
            verifyZeroMqTransmission(zeroMQSocket, logs)
        }

        private fun verifyZeroMqTransmission(zeroMQSocket: ZMQ.Socket, sentLogs: List<LogBatchDTO>) {
            val serializedLogs = List(sentLogs.size) { String(zeroMQSocket.recv()) }
            val receivedLogs =
                serializedLogs.map { topicBatchSerializer.deserialize(it, LogBatchDTO::class.java) }

            // num. sent logs = num. received logs
            Assertions.assertEquals(receivedLogs.size, sentLogs.size)
            Assertions.assertEquals(sentLogs, receivedLogs)
        }
    }
}
