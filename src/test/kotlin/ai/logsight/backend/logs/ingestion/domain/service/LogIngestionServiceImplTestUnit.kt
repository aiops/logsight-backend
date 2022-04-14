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
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogStream
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
internal class LogIngestionServiceImplTestUnit {
    @Mock
    private lateinit var topicBuilder: TopicBuilder

    @Mock
    private lateinit var logsReceiptStorageService: LogsReceiptStorageService

    @Mock
    private lateinit var applicationStorageService: ApplicationStorageService

    @Mock
    private lateinit var applicationLifecycleServiceImpl: ApplicationLifecycleServiceImpl

    @Mock
    private lateinit var logStream: LogStream

    @Spy
    private val xSync = XSync<String>()

    @InjectMocks
    private lateinit var logIngestionServiceImpl: LogIngestionServiceImpl

    private val user = TestInputConfig.baseUser
    private val app = TestInputConfig.baseApp
    private val topic = "${user.key}_${app.name}_input"
    private val log = LogMessage(
        message = "Hello World!",
        timestamp = DateTime.now()
            .toString()
    )
    private val logStrings = listOf(log)
    private val orderCounter = 1L
    private val source = LogDataSources.REST_BATCH.source
    private val logObjects = createMockLogObjects(
        app.name, app.id.toString(), user.key, "default", orderCounter, logStrings
    )

    @Test
    fun `should return valid log receipt`() {
        // given
        app.status = ApplicationStatus.READY
        val createLogsReceiptCommand = CreateLogsReceiptCommand(logStrings.size, LogDataSources.REST_BATCH.name, app)
        val mockLogsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logStrings.size, source = LogDataSources.REST_BATCH.name
        )
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .doReturn(mockLogsReceipt)
        Mockito.`when`(logStream.serializeAndSend(topic, logObjects))
            .thenReturn(logObjects.size)

        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)
        // when
        val logsReceipt = logIngestionServiceImpl.processLogBatch(logBatch)

        // then
        assertNotNull(logsReceipt)
        assertEquals(logsReceipt.source, LogDataSources.REST_BATCH.name)
        assertEquals(logsReceipt.logsCount, logObjects.size)
        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
        assertEquals(logsReceipt.application, app)
    }

    @Test
    fun `should throw exception due to invalid app state creating`() {
        app.status = ApplicationStatus.CREATING
        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)

        val exception = assertFailsWith<ApplicationStatusException> {
            logIngestionServiceImpl.processLogBatch(logBatch)
        }
        assertNotNull(exception)
    }

    @Test
    fun `should throw exception due to invalid app state deleting`() {
        app.status = ApplicationStatus.DELETING
        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)
        val exception = assertFailsWith<ApplicationStatusException> {
            logIngestionServiceImpl.processLogBatch(logBatch)
        }
        assertNotNull(exception)
    }

    @Test
    fun `should throw an runtime exceptions if logs receipt storage returns null`() {
        app.status = ApplicationStatus.READY
        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)
        val createLogsReceiptCommand = CreateLogsReceiptCommand(logStrings.size, LogDataSources.REST_BATCH.source, app)

        // given
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, app.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(null)

        // when
        val exception = assertFailsWith<RuntimeException> { logIngestionServiceImpl.processLogBatch(logBatch) }

        // then
        assertNotNull(exception)
    }

    @Test
    fun `should forward internal exceptions from the logs receipt storage`() {
        // given
        app.status = ApplicationStatus.READY
        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)
        val createLogsReceiptCommand = CreateLogsReceiptCommand(logStrings.size, LogDataSources.REST_BATCH.source, app)

        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, app.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenThrow(RuntimeException::class.java)
        // when
        val exception = assertFailsWith<RuntimeException> { logIngestionServiceImpl.processLogBatch(logBatch) }
        // then
        assertNotNull(exception)
    }

    @Test
    fun `number of received logs not equal to number of transmitted logs`() {
        // given
        app.status = ApplicationStatus.READY
        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)
        val createLogsReceiptCommand = CreateLogsReceiptCommand(logStrings.size, LogDataSources.REST_BATCH.name, app)
        val mockLogsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logStrings.size, source = LogDataSources.REST_BATCH.name
        )
        val logsReceiptMismatch = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logStrings.size - 1, source = source
        )

        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(mockLogsReceipt)
        Mockito.`when`(logStream.serializeAndSend(topic, logObjects))
            .thenReturn(logObjects.size - 1)
        Mockito.`when`(logsReceiptStorageService.updateLogsCount(mockLogsReceipt, logObjects.size - 1))
            .thenReturn(logsReceiptMismatch)

        // when
        val logsReceipt = logIngestionServiceImpl.processLogBatch(logBatch)
        // then
        assertNotNull(logsReceipt)
        assertEquals(logsReceipt.source, source)
        assertEquals(logObjects.size - 1, logsReceipt.logsCount)
        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
        assertEquals(logsReceipt.application, app)
    }

    @Test
    fun `should forward internal exceptions from the logs receipt update`() {
        // given
        app.status = ApplicationStatus.READY
        val logBatch = createMockLogBatchDTO(user, app, logs = logStrings)
        val createLogsReceiptCommand = CreateLogsReceiptCommand(logStrings.size, LogDataSources.REST_BATCH.source, app)
        val mockLogsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logStrings.size, source = LogDataSources.REST_BATCH.source
        )
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, app.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(mockLogsReceipt)
        Mockito.`when`(logStream.serializeAndSend(topic, logObjects))
            .thenReturn(logObjects.size - 1)
        Mockito.`when`(logsReceiptStorageService.updateLogsCount(mockLogsReceipt, logObjects.size - 1))
            .thenThrow(RuntimeException::class.java)

        // when
        val exception = assertFailsWith<Exception> { logIngestionServiceImpl.processLogBatch(logBatch) }

        // then
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
