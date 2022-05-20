package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.extensions.toApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.stubbing.Answer
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
@DirtiesContext
internal class LogIngestionServiceImplUnitTest {

    @Mock
    private lateinit var applicationStorageService: ApplicationStorageService

    @Mock
    private lateinit var applicationLifecycleService: ApplicationLifecycleService

    @Mock
    private lateinit var logsReceiptStorageService: LogsReceiptStorageService

    @Mock
    private lateinit var logSink: LogSink

    @InjectMocks
    private lateinit var logIngestionServiceImpl: LogIngestionServiceImpl

    @Test
    fun `should return valid log receipt for logBatch`() {
        // given
        val appReady = TestInputConfig.baseApp.toApplicationStatus(ApplicationStatus.READY)
        val createLogsReceiptCommand = CreateLogsReceiptCommand(TestInputConfig.logBatch.logs.size, appReady)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .doReturn(TestInputConfig.logsReceipt)
        // when

        val logsReceipt = logIngestionServiceImpl.processLogBatch(TestInputConfig.logBatch)

        // then
        assertNotNull(logsReceipt)
        assertEquals(TestInputConfig.logBatch.logs.size, logsReceipt.logsCount)
        assertEquals(1, logsReceipt.orderNum)
        assertEquals(appReady, logsReceipt.application)
    }

    @Nested
    @DisplayName("Process Log Events")
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    inner class ProcessLogEvents {

        @Test
        fun `should return valid log receipt for log events by id`() {
            // given
            val logEventsDTOById = TestInputConfig.logEventsDTOById
            val appReady = TestInputConfig.getAppWithStatus(ApplicationStatus.READY)
            Mockito.`when`(applicationStorageService.findApplicationById(appReady.id))
                .doReturn(appReady)
            runLogEventsTest(logEventsDTOById, TestInputConfig.getLogsReceipts(1), appReady)
        }

        @Test
        fun `should return valid log receipt for log events by name`() {
            // given
            val logEventsDTOByName = TestInputConfig.logEventsDTOByName
            val appReady = TestInputConfig.getAppWithStatus(ApplicationStatus.READY)
            Mockito.`when`(applicationLifecycleService.autoCreateApplication(TestInputConfig.createApplicationCommand))
                .doReturn(appReady)
            runLogEventsTest(logEventsDTOByName, TestInputConfig.getLogsReceipts(1), appReady)
        }

        @Test
        fun `should return valid log receipt for log events mixed`() {
            // given
            val logEventsDTOMixed = TestInputConfig.logEventsDTOMixed
            val appReady = TestInputConfig.getAppWithStatus(ApplicationStatus.READY)
            Mockito.`when`(applicationStorageService.findApplicationById(appReady.id))
                .doReturn(appReady)
            Mockito.`when`(applicationLifecycleService.autoCreateApplication(TestInputConfig.createApplicationCommand))
                .doReturn(appReady)
            runLogEventsTest(logEventsDTOMixed, TestInputConfig.getLogsReceipts(2), appReady)
        }
        
        private fun runLogEventsTest(logEventsDTO: LogEventsDTO, expectedLogsReceipts: List<LogsReceipt>, appReady: Application) {
            // given
            val createLogsReceiptCommand = CreateLogsReceiptCommand(TestInputConfig.numMessages, appReady)
            Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand)).thenAnswer(
                object : Answer<LogsReceipt> {
                    private var i = 0
                    override fun answer(invocation: InvocationOnMock?): LogsReceipt {
                        return expectedLogsReceipts[i++]
                    }
                }
            )

            // when
            val logsReceipts = logIngestionServiceImpl.processLogEvents(logEventsDTO)

            // then
            assertNotNull(logsReceipts)
            assertEquals(expectedLogsReceipts.size, logsReceipts.size)
            var counter: Long = 1
            logsReceipts.forEach {
                assertEquals(TestInputConfig.numMessages, it.logsCount)
                assertEquals(counter++, it.orderNum)
                assertEquals(appReady, it.application)
            }
        }
    }
}
