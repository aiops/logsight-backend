package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.extensions.toApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
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
    fun `should return valid log receipt`() {
        // given
        val appReady = TestInputConfig.getAppWithStatus(ApplicationStatus.READY)
        val createLogsReceiptCommand = CreateLogsReceiptCommand(TestInputConfig.logBatch.logs.size, appReady)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .doReturn(TestInputConfig.logReceipt)
        // when

        val logsReceipt = logIngestionServiceImpl.processLogBatch(TestInputConfig.logBatch)

        // then
        assertNotNull(logsReceipt)
        assertEquals(logsReceipt.logsCount, TestInputConfig.logBatch.logs.size)
        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
        assertEquals(logsReceipt.application, appReady)
    }
}
