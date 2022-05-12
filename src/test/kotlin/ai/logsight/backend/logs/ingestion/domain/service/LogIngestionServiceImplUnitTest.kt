// package ai.logsight.backend.logs.ingestion.domain.service
//
// import ai.logsight.backend.TestInputConfig
// import ai.logsight.backend.TestInputConfig.logBatch
// import ai.logsight.backend.application.domain.Application
// import ai.logsight.backend.application.domain.ApplicationStatus
// import ai.logsight.backend.application.exceptions.ApplicationStatusException
// import ai.logsight.backend.application.extensions.toApplicationStatus
// import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
// import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
// import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
// import org.junit.jupiter.api.Test
// import org.junit.jupiter.api.extension.ExtendWith
// import org.mockito.InjectMocks
// import org.mockito.Mock
// import org.mockito.Mockito
// import org.mockito.junit.jupiter.MockitoExtension
// import org.mockito.kotlin.doReturn
// import org.springframework.test.annotation.DirtiesContext
// import java.util.*
// import kotlin.test.assertEquals
// import kotlin.test.assertFailsWith
// import kotlin.test.assertNotNull
//
// @ExtendWith(MockitoExtension::class)
// @DirtiesContext
// internal class LogIngestionServiceImplUnitTest {
//    @Mock
//    private lateinit var logsReceiptStorageService: LogsReceiptStorageService
//
//    @InjectMocks
//    private lateinit var logIngestionServiceImpl: LogIngestionServiceImpl
//
//    @Test
//    fun `should return valid log receipt`() {
//        // given
//        val appReady = TestInputConfig.baseApp.toApplicationStatus(ApplicationStatus.READY)
//        val createLogsReceiptCommand = CreateLogsReceiptCommand(logs.size, app)
//        val mockLogsReceipt = createMockLogsReceipt(
//            app, orderCounter = orderCounter, logsCount = logs.size,
//        )
//        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
//            .doReturn(mockLogsReceipt)
//
//        // when
//        val logsReceipt = logIngestionServiceImpl.processLogBatch(logBatch)
//
//        // then
//        assertNotNull(logsReceipt)
//        assertEquals(logsReceipt.logsCount, logObjects.size)
//        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
//        assertEquals(logsReceipt.application, app)
//    }
//
//    @Test
//    fun `should throw exception due to invalid app state creating`() {
//        val app = app.toApplicationStatus(ApplicationStatus.CREATING)
//
//        val exception = assertFailsWith<ApplicationStatusException> {
//            logIngestionServiceImpl.processLogBatch(logBatch)
//        }
//        assertNotNull(exception)
//    }
//
//    @Test
//    fun `should throw exception due to invalid app state deleting`() {
//        val app = app.toApplicationStatus(ApplicationStatus.DELETING)
//        val exception = assertFailsWith<ApplicationStatusException> {
//            logIngestionServiceImpl.processLogBatch(logBatch)
//        }
//        assertNotNull(exception)
//    }
//
//    private fun createMockLogsReceipt(
//        application: Application,
//        id: UUID = UUID.randomUUID(),
//        orderCounter: Long = 1,
//        logsCount: Int = 1,
//        source: String = "restBatch"
//    ) = LogsReceipt(id, orderCounter, logsCount, application)
// }
