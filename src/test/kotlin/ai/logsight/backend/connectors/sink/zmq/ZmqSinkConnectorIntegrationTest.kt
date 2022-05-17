package ai.logsight.backend.connectors.sink.zmq

import ai.logsight.backend.connectors.sink.zmq.config.ZmqConfigProperties
import com.sun.mail.iap.ConnectionException
import org.junit.jupiter.api.*
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
internal class ZmqSinkConnectorIntegrationTest {
    @Autowired
    lateinit var zmqSinkConnector: ZmqSinkConnector

    @Autowired
    lateinit var zmqConfigProperties: ZmqConfigProperties

    @Nested
    @DisplayName("Send Logs via zmq")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SendLogs {

        private fun getZeroMqTestSocket(): ZMQ.Socket {
            val ctx = ZContext()
            val zeroMQSocket = ctx.createSocket(SocketType.SUB)
            val addr = "${zmqConfigProperties.protocol}://0.0.0.0:${zmqConfigProperties.port}"
            val status = zeroMQSocket.connect(addr)
            if (!status) throw ConnectionException("Test ZeroMQ SUB is not able to connect socket to $addr")
            zeroMQSocket.subscribe("")
            Thread.sleep(5000)
            return zeroMQSocket
        }

        @Test
        fun `should send log to zeromq success`() {
            // given
            val msg = "Hello world"

            // when
            val success = zmqSinkConnector.send(msg)

            // then
            Assertions.assertTrue(success)
        }

        @Test
        fun `test transmission via zeromq`() {
            // given
            val numMsg = 1000
            val msg = "Hello world"
            val messages = List(numMsg) { msg }
            val zeroMQSocket = getZeroMqTestSocket()

            // when
            val successes = messages.map {
                zmqSinkConnector.send(it)
            }

            // then
            Assertions.assertEquals(successes.size, numMsg)
            verifyZeroMqTransmission(zeroMQSocket, messages)
        }

        private fun verifyZeroMqTransmission(zeroMQSocket: ZMQ.Socket, sentMessages: List<String>) {
            val receivedMessages = List(sentMessages.size) { String(zeroMQSocket.recv()) }

            // num. sent logs = num. received logs
            Assertions.assertEquals(receivedMessages.size, sentMessages.size)
            Assertions.assertEquals(sentMessages, receivedMessages)
        }
    }
}
