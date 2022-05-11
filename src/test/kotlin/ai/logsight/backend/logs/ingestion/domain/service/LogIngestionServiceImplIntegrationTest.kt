package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.rpc.RPCService
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.common.utils.TopicJsonSerializer
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogSinglesDTO
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogQueue
import ai.logsight.backend.logs.ingestion.ports.out.stream.adapters.zeromq.config.LogStreamZeroMqConfigProperties
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

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

    @MockBean
    private lateinit var applicationRPCServiceZeroMq: RPCService

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
            logIngestionServiceImpl.logQueue.blockingLogQueue.clear()
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
            val numLogs = 5
            val batches = List(numLogs) { logMessages }

            // when
            val logReceipts = batches.map { batch ->
                logIngestionServiceImpl.processLogBatch(
                    LogBatchDTO(
                        TestInputConfig.baseUser, application1, tag, batch, source = source
                    )
                )
            }

            // then
            Assertions.assertEquals(logReceipts.size, numLogs)
            // assert that values are sorted asc
            Assertions.assertTrue {
                logReceipts.map { it.orderNum }
                    .asSequence()
                    .zipWithNext { a, b -> a <= b }
                    .all { it }
            }
        }

        @Test
        fun `should maintain a specific order when adding the logs to the queue`() {
            // given
            val numLogs = 5
            val batches = List(numLogs) { logMessages }

            // when
            val logsReceipts = batches.map { batch ->
                logIngestionServiceImpl.processLogBatch(
                    LogBatchDTO(
                        TestInputConfig.baseUser, application1, tag, batch, source = source
                    )
                )
            }

            // then
            verifyLogOrderInQueue(logIngestionServiceImpl.logQueue, logsReceipts)
        }

        @Test
        fun `should maintain a specific order when adding the logs to the queue concurrency test`() {
            // given
            val numLogs = 20
            val batches = List(numLogs) { logMessages }

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
            verifyLogOrderInQueue(logIngestionServiceImpl.logQueue, logsReceipts)
        }

        @Test
        fun `should maintain a specific order when adding the logs to the queue concurrency test with different apps`() {
            // given
            val numLogs = 5
            val batches = List(numLogs) { logMessages }

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
            verifyLogOrderInQueue(logIngestionServiceImpl.logQueue, logsReceipts1)
            verifyLogOrderInQueue(logIngestionServiceImpl.logQueue, logsReceipts2)
        }

        private fun verifyLogOrderInQueue(queue: LogQueue, logsReceipts: List<LogsReceipt>) {
            val receiptIdsExpected = logsReceipts.map { it.orderNum }
            val receiptIdsActual = queue.blockingLogQueue.map { it.second.orderCounter }.toList()

            // num. sent logs = num. received logs
            Assertions.assertEquals(receiptIdsExpected.size, logsReceipts.size)
            // assert that values are sorted asc
            Assertions.assertTrue {
                receiptIdsActual.asSequence()
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

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogsSingles {

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

        private val logMessage = SendLogMessage(
            message = "Hello World!",
            timestamp = DateTime.now().toString(),
            applicationId = application1.id,
            tag = "default"
        )
        private val logMessages = List(numMessages) { logMessage }

        @Test
        fun `should return valid log receipt`() {
            // given
            val logBatchSinglesDTO = LogSinglesDTO(
                user = TestInputConfig.baseUser, logs = logMessages, source = source
            )
            // when
            val logReceipts = logIngestionServiceImpl.processLogSingles(logBatchSinglesDTO)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(numMessages, logReceipts[0].logsCount)
            Assertions.assertEquals(source.name, logReceipts[0].source)
            Assertions.assertEquals(application1.id, logReceipts[0].application.id)
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.delete(applicationEntity1)
            applicationRepository.delete(applicationEntity2)
        }
    }

    @Nested
    @WithMockUser(username = TestInputConfig.baseEmail)
    @DisplayName("Process LogSingles Auto")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogsSinglesAuto {

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

        private val logMessage = SendLogMessage(
            applicationName = "test_app1",
            message = "Hello World!",
            timestamp = DateTime.now().toString(),
            tag = "default"
        )
        private val logMessages = List(numMessages) { logMessage }

        @Test
        fun `should return valid log receipt`() {
            // given
            val logBatchSinglesDTO = LogSinglesDTO(
                user = TestInputConfig.baseUser, logs = logMessages, source = source
            )
            // when
            val logReceipts = logIngestionServiceImpl.processLogSingles(logBatchSinglesDTO)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(numMessages, logReceipts[0].logsCount)
            Assertions.assertEquals(source.name, logReceipts[0].source)
            Assertions.assertEquals(application1.id, logReceipts[0].application.id)
        }

        @Test
        fun `should return valid log receipt when application name does not exists, it should create the app first`() {
            // given
            val logMessage =
                SendLogMessage(
                    applicationName = "test_app_new_name",
                    message = "Hello World!",
                    timestamp = DateTime.now().toString(),
                    tag = "default"
                )
            val logMessages = List(numMessages) { logMessage }
            val logBatchSinglesDTO = LogSinglesDTO(
                user = TestInputConfig.baseUser, logs = logMessages, source = source
            )

            val response = RPCResponse(
                TestInputConfig.baseAppEntity.id.toString(), "message", 200
            )
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenReturn(response)
            // when
            val logReceipts = logIngestionServiceImpl.processLogSingles(logBatchSinglesDTO)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(numMessages, logReceipts[0].logsCount)
            Assertions.assertEquals(source.name, logReceipts[0].source)
            Assertions.assertEquals("test_app_new_name", logReceipts[0].application.name)
        }

        @Test
        fun `should return valid receipt when applicationId or applicationName are in the request`() {
            // given
            val logMessage1 = listOf(
                SendLogMessage(
                    applicationName = "test_app_new_name",
                    message = "Hello World!",
                    timestamp = DateTime.now()
                        .toString(),
                    tag = "default"
                )
            )

            val logMessage2 = listOf(
                SendLogMessage(
                    applicationId = application1.id,
                    message = "Hello World!",
                    timestamp = DateTime.now()
                        .toString(),
                    tag = "default"
                )
            )

            val logMessages = logMessage1 + logMessage2
            val logBatchSinglesDTO = LogSinglesDTO(
                user = TestInputConfig.baseUser, logs = logMessages, source = source
            )

            val response = RPCResponse(
                TestInputConfig.baseAppEntity.id.toString(), "message", 200
            )
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenReturn(response)
            // when
            val logReceipts = logIngestionServiceImpl.processLogSingles(logBatchSinglesDTO)
            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(logMessages.size, logReceipts.size)
            Assertions.assertEquals(source.name, logReceipts[1].source)
        }

        @Test
        fun `should return error when application is not in READY state`() {
            // given
            val logMessage =
                SendLogMessage(
                    applicationName = "test_app_new_app",
                    message = "Hello World!",
                    timestamp = DateTime.now()
                        .toString(),
                    tag = "default"
                )
            val logMessages = List(numMessages) { logMessage }
            val logBatchSinglesDTO = LogSinglesDTO(
                user = TestInputConfig.baseUser, logs = logMessages, source = source
            )

            val response = RPCResponse(
                "", "Timeout", 400
            )
            Mockito.`when`(applicationRPCServiceZeroMq.createApplication(any()))
                .thenReturn(response)

            // when
            val exception = assertFailsWith<ApplicationNotFoundException> {
                logIngestionServiceImpl.processLogSingles(logBatchSinglesDTO)
            }

            // then
            assertNotNull(exception)
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.delete(applicationEntity1)
            applicationRepository.delete(applicationEntity2)
        }
    }
}
