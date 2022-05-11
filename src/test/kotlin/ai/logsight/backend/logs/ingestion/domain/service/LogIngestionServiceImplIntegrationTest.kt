package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.TestInputConfig.defaultTag
import ai.logsight.backend.TestInputConfig.logBatch
import ai.logsight.backend.TestInputConfig.sendLogMessage
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

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

    companion object {
        private const val numBatches = 10
        val batches = List(numBatches) { logBatch }
    }

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        @BeforeAll
        fun setupAll() {
            userRepository.save(TestInputConfig.baseUserEntity)
            applicationRepository.save(TestInputConfig.baseAppEntity)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
//            logIngestionServiceImpl.logQueue.blockingLogQueue.clear()
        }

        @Test
        fun `should return valid log receipt`() {
            // given

            // when
            val logReceipt = logIngestionServiceImpl.processLogBatch(logBatch)

            // then
            Assertions.assertNotNull(logReceipt)
            Assertions.assertEquals(TestInputConfig.numMessages, logReceipt.logsCount)
            Assertions.assertEquals(TestInputConfig.baseApp.id, logReceipt.application.id)
        }

        @Test
        fun `should return ordered order counter`() {
            // given

            // when
            val logReceipts = batches.map { batch ->
                logIngestionServiceImpl.processLogBatch(
                    batch
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

        @AfterAll
        fun teardown() {
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.deleteAll()
        }
    }

    @Nested
    @DisplayName("Process Log Singles")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogsSingles {

        @BeforeAll
        fun setupAll() {
            userRepository.save(TestInputConfig.baseUserEntity)
            applicationRepository.save(TestInputConfig.baseAppEntity)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
        }

        private val logMessages = List(TestInputConfig.numMessages) { sendLogMessage }

        @Test
        fun `should return valid log receipt`() {
            // given
            val logBatchSinglesDTO = LogEventsDTO(
                user = TestInputConfig.baseUser, logs = logMessages
            )
            // when
            val logReceipts = logIngestionServiceImpl.processLogEvents(logBatchSinglesDTO)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(TestInputConfig.numMessages, logReceipts[0].logsCount)
            Assertions.assertEquals(TestInputConfig.baseApp.id, logReceipts[0].application.id)
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.deleteAll()
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
            applicationRepository.save(TestInputConfig.baseAppEntity)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
        }

        private val logMessages = List(TestInputConfig.numMessages) { sendLogMessage }

        @Test
        fun `should return valid log receipt`() {
            // given
            val logBatchSinglesDTO = LogEventsDTO(
                user = TestInputConfig.baseUser, logs = logMessages
            )
            // when
            val logReceipts = logIngestionServiceImpl.processLogEvents(logBatchSinglesDTO)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(TestInputConfig.numMessages, logReceipts[0].logsCount)
            Assertions.assertEquals(TestInputConfig.baseApp.id, logReceipts[0].application.id)
        }

        @Test
        fun `should return valid log receipt when application name does not exists, it should create the app first`() {
            // given
            val logMessage =
                SendLogMessage(
                    applicationName = "test_app_new_name",
                    message = "Hello World!",
                    timestamp = DateTime.now().toString(),
                    tags = defaultTag
                )
            val logMessages = List(TestInputConfig.numMessages) { logMessage }
            val logBatchSinglesDTO = LogEventsDTO(
                user = TestInputConfig.baseUser, logs = logMessages
            )

            // when
            val logReceipts = logIngestionServiceImpl.processLogEvents(logBatchSinglesDTO)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(TestInputConfig.numMessages, logReceipts[0].logsCount)
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
                    tags = defaultTag
                )
            )

            val logMessage2 = listOf(
                SendLogMessage(
                    applicationId = TestInputConfig.baseApp.id,
                    message = "Hello World!",
                    timestamp = DateTime.now()
                        .toString(),
                    tags = defaultTag
                )
            )

            val logMessages = logMessage1 + logMessage2
            val logBatchSinglesDTO = LogEventsDTO(
                user = TestInputConfig.baseUser, logs = logMessages
            )

            // when
            val logReceipts = logIngestionServiceImpl.processLogEvents(logBatchSinglesDTO)
            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(logMessages.size, logReceipts.size)
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.deleteAll()
        }
    }
}
