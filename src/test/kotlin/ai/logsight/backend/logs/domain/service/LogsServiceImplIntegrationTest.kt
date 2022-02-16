package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.domain.LogFormats
import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ports.out.stream.adapters.zeromq.config.LogStreamZeroMqConfigProperties
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.sun.mail.iap.ConnectionException
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
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

class LogsServiceImplIntegrationTest {
    @Autowired
    lateinit var applicationRepository: ApplicationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var logsReceiptRepository: LogsReceiptRepository

    @Autowired
    lateinit var logsServiceImpl: LogsServiceImpl

    @Autowired
    lateinit var topicJsonSerializer: TopicJsonSerializer

    @Autowired
    lateinit var topicBuilder: TopicBuilder

    @Autowired
    lateinit var zeroMqConf: LogStreamZeroMqConfigProperties

    companion object {
        private const val numMessages = 1000
        private const val logMessage = "Hello World"
        private const val source = "test"
        private const val tag = "default"
        private val format = LogFormats.UNKNOWN_FORMAT.toString()

        val logMessages = List(numMessages) { logMessage }

        val userEntity = UserEntity(
            email = "testemail@mail.com",
            password = "testpassword",
            userType = UserType.ONLINE_USER

        )
        val user = userEntity.toUser()

        val applicationEntity1 = ApplicationEntity(
            name = "testapp1",
            status = ApplicationStatus.READY,
            user = user.toUserEntity()
        )
        private val application1 = applicationEntity1.toApplication()

        val applicationEntity2 = ApplicationEntity(
            name = "testapp2",
            status = ApplicationStatus.READY,
            user = user.toUserEntity()
        )
        private val application2 = applicationEntity2.toApplication()

        // The IDE warning can be ignored
        private val threadPoolContext = newFixedThreadPoolContext(20, "Log Process")
    }

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        @BeforeAll
        fun setupAll() {
            userRepository.save(userEntity)
            applicationRepository.save(applicationEntity1)
            applicationRepository.save(applicationEntity2)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
        }

        private fun getZeroMqTestSocket(topic: String): ZMQ.Socket {
            val ctx = ZContext()
            val zeroMQSocket = ctx.createSocket(SocketType.SUB)
            val addr = "${zeroMqConf.protocol}://${zeroMqConf.host}:${zeroMqConf.port}"
            val status = zeroMQSocket.connect(addr)
            if (!status) throw ConnectionException("Test ZeroMQ SUB is not able to connect socket to $addr")
            zeroMQSocket.subscribe(topic)
            return zeroMQSocket
        }

        @Test
        fun `should return valid log receipt`() {
            // given

            // when
            val logReceipt = logsServiceImpl.processLogs(user, application1, format, tag, source, logMessages)

            // then
            Assertions.assertNotNull(logReceipt)
            Assertions.assertEquals(numMessages, logReceipt.logsCount)
            Assertions.assertEquals(source, logReceipt.source)
            Assertions.assertEquals(application1.id, logReceipt.application.id)
        }

        @Test
        fun `should return ordered order counter`() {
            // given
            val numBatches = 5
            val batches = List(numBatches) { logMessages }

            // when
            val logReceipts = batches.map { batch ->
                logsServiceImpl.processLogs(user, application1, format, tag, source, batch)
            }

            // then
            Assertions.assertEquals(logReceipts.size, numBatches)
            // assert that values are sorted asc
            Assertions.assertTrue {
                logReceipts.map { it.orderNum }
                    .asSequence()
                    .zipWithNext { a, b -> a <= b }
                    .all { it }
            }
        }

        @Test
        fun `should transmit order to zeromq`() {
            // given
            val topic = topicBuilder.buildTopic(listOf(user.key, application1.name))
            val zeroMQSocket = getZeroMqTestSocket(topic)

            val numBatches = 5
            val batches = List(numBatches) { logMessages }

            // when
            val logsReceipts = batches.map { batch ->
                logsServiceImpl.processLogs(user, application1, format, tag, source, batch)
            }

            // then
            verifyZeroMqOrder(zeroMQSocket, logsReceipts, numBatches)
            zeroMQSocket.close()
        }

        @Test
        fun `should transmit order to zeromq concurrent`() {
            // given
            val topic = topicBuilder.buildTopic(listOf(user.key, application1.name))
            val zeroMQSocket = getZeroMqTestSocket(topic)

            val numBatches = 20
            val batches = List(numBatches) { logMessages }

            // when
            val logsReceipts = java.util.Collections.synchronizedList(mutableListOf<LogsReceipt>())
            runBlocking(threadPoolContext) {
                batches.forEach { batch ->
                    launch {
                        val logsReceipt = logsServiceImpl.processLogs(user, application1, format, tag, source, batch)
                        logsReceipts.add(logsReceipt)
                    }
                }
            }

            // then
            verifyZeroMqOrder(zeroMQSocket, logsReceipts, numBatches)
            zeroMQSocket.close()
        }

        @Test
        fun `should not block zeromq concurrent transfer for different apps`() {
            // given
            val topic1 = topicBuilder.buildTopic(listOf(user.key, application1.name))
            val topic2 = topicBuilder.buildTopic(listOf(user.key, application2.name))
            val zeroMQSocket1 = getZeroMqTestSocket(topic1)
            val zeroMQSocket2 = getZeroMqTestSocket(topic2)

            val numBatches = 5
            val batches = List(numBatches) { logMessages }

            // when
            val logsReceipts1 = java.util.Collections.synchronizedList(mutableListOf<LogsReceipt>())
            val logsReceipts2 = java.util.Collections.synchronizedList(mutableListOf<LogsReceipt>())
            runBlocking(threadPoolContext) {
                batches.forEach { batch ->
                    launch {
                        val logsReceipt = logsServiceImpl.processLogs(user, application1, format, tag, source, batch)
                        logsReceipts1.add(logsReceipt)
                    }
                    launch {
                        val logsReceipt = logsServiceImpl.processLogs(user, application2, format, tag, source, batch)
                        logsReceipts2.add(logsReceipt)
                    }
                }
            }

            // then
            verifyZeroMqOrder(zeroMQSocket1, logsReceipts1, numBatches)
            verifyZeroMqOrder(zeroMQSocket2, logsReceipts2, numBatches)
        }

        private fun verifyZeroMqOrder(zeroMQSocket: ZMQ.Socket, logsReceipts: List<LogsReceipt>, numBatches: Int) {
            val receiptIdsExpected = logsReceipts.map { it.orderNum }
            val serializedLogs = List(numBatches * numMessages) {
                String(zeroMQSocket.recv())
            }
            val receiptIds = serializedLogs.map { topicJsonSerializer.deserialize(it, Log::class.java).orderCounter }

            // num. sent logs = num. received logs
            Assertions.assertEquals(receiptIdsExpected.size, logsReceipts.size)
            // assert that values are sorted asc
            Assertions.assertTrue {
                receiptIds.asSequence()
                    .zipWithNext { a, b -> a <= b }
                    .all { it }
            }
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(userEntity)
            applicationRepository.delete(applicationEntity1)
            applicationRepository.delete(applicationEntity2)
        }
    }
}
