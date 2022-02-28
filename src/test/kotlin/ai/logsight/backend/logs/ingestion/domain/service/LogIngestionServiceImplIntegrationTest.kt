package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq.config.LogStreamZeroMqConfigProperties
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.sun.mail.iap.ConnectionException
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
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

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@DirtiesContext
class LogIngestionServiceImplIntegrationTest {
    @Autowired
    lateinit var applicationRepository: ApplicationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var logsReceiptRepository: LogsReceiptRepository

    @Autowired
    lateinit var logIngestionServiceImpl: LogIngestionServiceImpl

    @Autowired
    lateinit var topicJsonSerializer: TopicJsonSerializer

    @Autowired
    lateinit var zeroMqConf: LogStreamZeroMqConfigProperties

    companion object {
        private const val numMessages = 1000
        private val logMessage = LogMessage(
            message = "Hello World!",
            timestamp = DateTime.now()
                .toString()
        )
        private val source = LogDataSources.REST_BATCH
        private const val tag = "default"
        val topicBuilder = TopicBuilder()

        val logMessages = List(numMessages) { logMessage }

        val applicationEntity1 = ApplicationEntity(
            name = "test_app1", status = ApplicationStatus.READY, user = TestInputConfig.baseUserEntity
        )
        private val application1 = applicationEntity1.toApplication()

        val applicationEntity2 = ApplicationEntity(
            name = "test_app2", status = ApplicationStatus.READY, user = TestInputConfig.baseUserEntity
        )
        private val application2 = applicationEntity2.toApplication()

        // The IDE warning can be ignored
        private val threadPoolContext = newFixedThreadPoolContext(20, "LogRequest Process")
    }

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        @BeforeAll
        fun setupAll() {
            userRepository.save(TestInputConfig.baseUserEntity)
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
            val addr = "${zeroMqConf.protocol}://0.0.0.0:${zeroMqConf.port}"
            val status = zeroMQSocket.connect(addr)
            if (!status) throw ConnectionException("Test ZeroMQ SUB is not able to connect socket to $addr")
            zeroMQSocket.subscribe(topic)
            Thread.sleep(3)
            return zeroMQSocket
        }

        @Test
        fun `should return valid log receipt`() {
            // given
            val logBatchDTO = LogBatchDTO(
                TestInputConfig.baseUser, application1, tag, logMessages, source = source
            )
            // when
            val logReceipt = logIngestionServiceImpl.processLogBatch(logBatchDTO)

            // then
            Assertions.assertNotNull(logReceipt)
            Assertions.assertEquals(numMessages, logReceipt.logsCount)
            Assertions.assertEquals(source.name, logReceipt.source)
            Assertions.assertEquals(application1.id, logReceipt.application.id)
        }

        @Test
        fun `should return ordered order counter`() {
            // given
            val numBatches = 5
            val batches = List(numBatches) { logMessages }

            // when
            val logReceipts = batches.map { batch ->
                logIngestionServiceImpl.processLogBatch(
                    LogBatchDTO(
                        TestInputConfig.baseUser, application1, tag, batch, source = source
                    )
                )
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
            val topic = topicBuilder.buildTopic(listOf(TestInputConfig.baseUser.key, application1.name))
            val zeroMQSocket = getZeroMqTestSocket(topic)

            val numBatches = 5
            val batches = List(numBatches) { logMessages }

            // when
            val logsReceipts = batches.map { batch ->
                logIngestionServiceImpl.processLogBatch(
                    LogBatchDTO(
                        TestInputConfig.baseUser, application1, tag, batch, source = source
                    )
                )
            }

            // then
            verifyZeroMqOrder(zeroMQSocket, logsReceipts, numBatches)
            zeroMQSocket.close()
        }

        @Test
        fun `should transmit order to zeromq concurrent`() {
            // given
            val topic = topicBuilder.buildTopic(listOf(TestInputConfig.baseUser.key, application1.name))
            val zeroMQSocket = getZeroMqTestSocket(topic)

            val numBatches = 20
            val batches = List(numBatches) { logMessages }

            // when
            val logsReceipts = java.util.Collections.synchronizedList(mutableListOf<LogsReceipt>())
            runBlocking(threadPoolContext) {
                batches.forEach { batch ->
                    launch {
                        val logsReceipt = logIngestionServiceImpl.processLogBatch(
                            LogBatchDTO(
                                TestInputConfig.baseUser,
                                application1,
                                tag,
                                batch,
                                source = source
                            )
                        )
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
            val topic1 = topicBuilder.buildTopic(listOf(TestInputConfig.baseUser.key, application1.name))
            val topic2 = topicBuilder.buildTopic(listOf(TestInputConfig.baseUser.key, application2.name))
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
                        val logsReceipt = logIngestionServiceImpl.processLogBatch(
                            LogBatchDTO(
                                TestInputConfig.baseUser,
                                application1,
                                tag,
                                batch,
                                source = source
                            )
                        )
                        logsReceipts1.add(logsReceipt)
                    }
                    launch {
                        val logsReceipt = logIngestionServiceImpl.processLogBatch(
                            LogBatchDTO(
                                TestInputConfig.baseUser,
                                application2,
                                tag,
                                batch,
                                source = source
                            )
                        )
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
            val receiptIds =
                serializedLogs.map { topicJsonSerializer.deserialize(it, LogsightLog::class.java).orderCounter }

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
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.delete(applicationEntity1)
            applicationRepository.delete(applicationEntity2)
        }
    }
}
