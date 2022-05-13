package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.users.ports.out.persistence.UserRepository
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
    lateinit var logIngestionService: LogIngestionService

    @Nested
    @DisplayName("Process Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ProcessLogs {

        @BeforeAll
        fun setupAll() {
            userRepository.save(TestInputConfig.baseUserEntity)
            applicationRepository.save(TestInputConfig.baseAppEntityReady)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
        }

        @Test
        fun `should return valid log receipt`() {
            // given

            // when
            val logReceipt = logIngestionService.processLogBatch(TestInputConfig.logBatch)

            logsReceiptRepository.deleteAll()

            // then
            Assertions.assertNotNull(logReceipt)
            Assertions.assertEquals(TestInputConfig.numMessages, logReceipt.logsCount)
            Assertions.assertEquals(TestInputConfig.baseApp.id, logReceipt.application.id)
        }

        @Test
        fun `should return ordered order counter`() {
            // given
            val numBatches = 10
            val batches = List(numBatches) { TestInputConfig.logBatch }

            // when
            val logReceipts = batches.map { batch ->
                logIngestionService.processLogBatch(batch)
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
            applicationRepository.save(TestInputConfig.baseAppEntityReady)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
        }

        @Test
        fun `should return valid log receipt`() {
            // given

            // when
            val logReceipts = logIngestionService.processLogEvents(TestInputConfig.logBatchSinglesDTOById)

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
            applicationRepository.save(TestInputConfig.baseAppEntityReady)
        }

        @BeforeEach
        fun setupEach() {
            logsReceiptRepository.deleteAll()
        }

        @Test
        fun `should return valid log receipt`() {
            // given

            // when
            val logReceipts = logIngestionService.processLogEvents(TestInputConfig.logBatchSinglesDTOById)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(TestInputConfig.numMessages, logReceipts[0].logsCount)
            Assertions.assertEquals(TestInputConfig.baseApp.id, logReceipts[0].application.id)
        }

        @Test
        fun `should return valid log receipt when application name does not exists, it should create the app first`() {
            // given

            // when
            val logReceipts = logIngestionService.processLogEvents(TestInputConfig.logBatchSinglesDTOByName)

            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(1, logReceipts.size)
            Assertions.assertEquals(TestInputConfig.logBatchSinglesDTOByName.logs.size, logReceipts[0].logsCount)
            Assertions.assertEquals(TestInputConfig.nonExistentAppName, logReceipts[0].application.name)
        }

        @Test
        fun `should return valid receipt when applicationId or applicationName are in the request`() {
            // given

            // when
            val logReceipts = logIngestionService.processLogEvents(TestInputConfig.logBatchSinglesDTOMixed)
            // then
            Assertions.assertNotNull(logReceipts)
            Assertions.assertEquals(2, logReceipts.size)
            Assertions.assertEquals(TestInputConfig.logBatchSinglesDTOById.logs.size, logReceipts[0].logsCount)
            Assertions.assertEquals(TestInputConfig.logBatchSinglesDTOByName.logs.size, logReceipts[1].logsCount)
        }

        @AfterAll
        fun teardown() {
            userRepository.delete(TestInputConfig.baseUserEntity)
            applicationRepository.deleteAll()
        }
    }
}
