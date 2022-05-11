package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.ApplicationLifecycleServiceImpl
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogQueue
import ai.logsight.backend.users.domain.User
import com.antkorwin.xsync.XSync
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.springframework.test.annotation.DirtiesContext
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
@DirtiesContext
internal class LogIngestionServiceImplUnitTest {
    @Mock
    private lateinit var topicBuilder: TopicBuilder

    @Mock
    private lateinit var logsReceiptStorageService: LogsReceiptStorageService

    @Mock
    private lateinit var applicationStorageService: ApplicationStorageService

    @Mock
    private lateinit var applicationLifecycleServiceImpl: ApplicationLifecycleServiceImpl

    @Mock
    private lateinit var logQueue: LogQueue

    @Spy
    private val xSync = XSync<String>()

    @InjectMocks
    private lateinit var logIngestionServiceImpl: LogIngestionServiceImpl

    private val user = TestInputConfig.baseUser
    private val app = TestInputConfig.baseApp
    private val log = LogMessage(
        message = "Hello World!",
        timestamp = DateTime.now().toString()
    )
    private val logs = listOf(log)
    private val orderCounter = 1L
    private val source = LogDataSources.REST_BATCH.name
    private val logObjects = createMockLogObjects(
        app.name, app.id.toString(), user.key, "default", orderCounter, logs
    )

    @Test
    fun `should return valid log receipt`() {
        // given
        app.status = ApplicationStatus.READY
        val createLogsReceiptCommand = CreateLogsReceiptCommand(logs.size, LogDataSources.REST_BATCH.name, app)
        val mockLogsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logs.size, source = LogDataSources.REST_BATCH.name
        )
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .doReturn(mockLogsReceipt)

        val logBatch = createMockLogBatchDTO(user, app, logs = logs)
        // when
        val logsReceipt = logIngestionServiceImpl.processLogBatch(logBatch)

        // then
        assertNotNull(logsReceipt)
        assertEquals(logsReceipt.source, source)
        assertEquals(logsReceipt.logsCount, logObjects.size)
        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
        assertEquals(logsReceipt.application, app)
    }

    @Test
    fun `should throw exception due to invalid app state creating`() {
        app.status = ApplicationStatus.CREATING
        val logBatch = createMockLogBatchDTO(user, app, logs = logs)

        val exception = assertFailsWith<ApplicationStatusException> {
            logIngestionServiceImpl.processLogBatch(logBatch)
        }
        assertNotNull(exception)
    }

    @Test
    fun `should throw exception due to invalid app state deleting`() {
        app.status = ApplicationStatus.DELETING
        val logBatch = createMockLogBatchDTO(user, app, logs = logs)
        val exception = assertFailsWith<ApplicationStatusException> {
            logIngestionServiceImpl.processLogBatch(logBatch)
        }
        assertNotNull(exception)
    }

    private fun createMockLogsReceipt(
        application: Application,
        id: UUID = UUID.randomUUID(),
        orderCounter: Long = 1,
        logsCount: Int = 1,
        source: String = "restBatch"
    ) = LogsReceipt(id, orderCounter, logsCount, source, application)

    private fun createMockLogObjects(
        appName: String = "testapp",
        appId: String = "testappkey",
        userKey: String = "key",
        tag: String = "default",
        orderCounter: Long = 1,
        logs: List<LogMessage>
    ) = logs.map { log ->
        LogsightLog(appName, appId, userKey, source = LogDataSources.REST_BATCH, tag, orderCounter, log)
    }

    private fun createMockLogBatchDTO(
        user: User,
        application: Application,
        tag: String = "default",
        logs: List<LogMessage>
    ) = LogBatchDTO(user, application, tag, logs, LogDataSources.REST_BATCH)
}
