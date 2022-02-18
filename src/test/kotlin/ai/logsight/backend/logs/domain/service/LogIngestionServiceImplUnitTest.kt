package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.utils.TopicBuilder
import ai.logsight.backend.logs.domain.Log
import ai.logsight.backend.logs.domain.enums.LogFormats
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionServiceImpl
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.stream.LogStream
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.antkorwin.xsync.XSync
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class LogIngestionServiceImplUnitTest {

    @Mock
    private lateinit var topicBuilder: TopicBuilder

    @Mock
    private lateinit var logsReceiptStorageService: LogsReceiptStorageService

    @Mock
    private lateinit var logStream: LogStream

    @Mock
    private lateinit var applicationLifecycleService: ApplicationLifecycleService

    @Mock
    private lateinit var applicationStorageService: ApplicationStorageService

    @Spy
    private val xSync = XSync<String>()

    @InjectMocks
    private lateinit var logIngestionServiceImpl: LogIngestionServiceImpl

    private val email = "sasho@sasho.com"
    private val user = createMockUser(email)
    private val appId = UUID.randomUUID()
    private val appReady = createMockApplication(user = user, id = appId)
    private val appCreating = createMockApplication(user = user, id = appId, status = ApplicationStatus.CREATING)
    private val appDeleting = createMockApplication(user = user, id = appId, status = ApplicationStatus.DELETING)
    private val topic = "${user.key}_${appReady.name}_input"
    private val log = "Hello World!"
    private val logStrings = listOf(log)
    private val logBatchDTOAppReady = createMockLogBatchDTO(user, appReady, logs = logStrings)
    private val logBatchDTOAppCreating = createMockLogBatchDTO(user, appCreating, logs = logStrings)
    private val logBatchDTOAppDeleting = createMockLogBatchDTO(user, appDeleting, logs = logStrings)
    private val orderCounter = 1L
    private val source = LogDataSources.REST_BATCH.source
    private val createLogsReceiptCommand = createCreateLogsReceiptCommand(
        appReady, logsCount = logStrings.size, source = source
    )
    private val logsReceipt = createMockLogsReceipt(
        appReady, orderCounter = orderCounter, logsCount = logStrings.size, source = source
    )
    private val logsReceiptMismatch = createMockLogsReceipt(
        appReady, orderCounter = orderCounter, logsCount = logStrings.size - 1, source = source
    )
    private val logObjects = createMockLogObjects(
        appReady.name, appId.toString(), user.key, logBatchDTOAppReady.logFormat.toString(),
        logBatchDTOAppReady.tag, orderCounter, logStrings
    )

    @Test
    fun `should return valid log receipt`() {
        // given
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, appReady.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(logsReceipt)
        Mockito.`when`(logStream.serializeAndSend(topic, logObjects))
            .thenReturn(logObjects.size)

        // when
        val logsReceipt = logIngestionServiceImpl.processLogBatch(logBatchDTOAppReady)

        // then
        assertNotNull(logsReceipt)
        assertEquals(logsReceipt.source, source)
        assertEquals(logsReceipt.logsCount, logObjects.size)
        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
        assertEquals(logsReceipt.application, appReady)
    }

    @Test
    fun `should throw exception due to invalid app state creating`() {
        val exception = assertFailsWith<ApplicationStatusException> {
            logIngestionServiceImpl.processLogBatch(logBatchDTOAppCreating)
        }
        assertNotNull(exception)
    }

    @Test
    fun `should throw exception due to invalid app state deleting`() {
        val exception = assertFailsWith<ApplicationStatusException> {
            logIngestionServiceImpl.processLogBatch(logBatchDTOAppDeleting)
        }
        assertNotNull(exception)
    }

    @Test
    fun `should throw an runtime exceptions if logs receipt storage returns null`() {
        // given
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, appReady.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(null)

        // when
        val exception =
            assertFailsWith<RuntimeException> { logIngestionServiceImpl.processLogBatch(logBatchDTOAppReady) }

        // then
        assertNotNull(exception)
    }

    @Test
    fun `should forward internal exceptions from the logs receipt storage`() {
        // given
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, appReady.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenThrow(RuntimeException::class.java)
        // when
        val exception =
            assertFailsWith<RuntimeException> { logIngestionServiceImpl.processLogBatch(logBatchDTOAppReady) }
        // then
        assertNotNull(exception)
    }

    @Test
    fun `number of received logs not equal to number of transmitted logs`() {
        // given
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, appReady.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(logsReceipt)
        Mockito.`when`(logStream.serializeAndSend(topic, logObjects))
            .thenReturn(logObjects.size - 1)
        Mockito.`when`(logsReceiptStorageService.updateLogsCount(logsReceipt, logObjects.size - 1))
            .thenReturn(logsReceiptMismatch)

        // when
        val logsReceipt = logIngestionServiceImpl.processLogBatch(logBatchDTOAppReady)
        // then
        assertNotNull(logsReceipt)
        assertEquals(logsReceipt.source, source)
        assertEquals(logObjects.size - 1, logsReceipt.logsCount)
        assertEquals(logsReceipt.orderNum, logsReceipt.orderNum)
        assertEquals(logsReceipt.application, appReady)
    }

    @Test
    fun `should forward internal exceptions from the logs receipt update`() {
        // given
        Mockito.`when`(topicBuilder.buildTopic(listOf(user.key, appReady.name, "input")))
            .thenReturn(topic)
        Mockito.`when`(logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand))
            .thenReturn(logsReceipt)
        Mockito.`when`(logStream.serializeAndSend(topic, logObjects))
            .thenReturn(logObjects.size - 1)
        Mockito.`when`(logsReceiptStorageService.updateLogsCount(logsReceipt, logObjects.size - 1))
            .thenThrow(RuntimeException::class.java)

        // when
        val exception = assertFailsWith<Exception> { logIngestionServiceImpl.processLogBatch(logBatchDTOAppReady) }

        // then
        assertNotNull(exception)
    }

    private fun createMockUser(email: String) = User(
        id = UUID.randomUUID(),
        email = email,
        password = "sasho",
        key = "key",
        activationDate = LocalDateTime.now(),
        dateCreated = LocalDateTime.now(),
        hasPaid = true,
        usedData = 10,
        approachingLimit = false,
        availableData = 10000,
        activated = true,
        userType = UserType.ONLINE_USER
    )

    private fun createMockApplication(
        user: User,
        name: String = "sashoapp",
        id: UUID = UUID.randomUUID(),
        status: ApplicationStatus = ApplicationStatus.READY,
        applicationKey: String = "key"
    ) = Application(id = id, name = name, status = status, applicationKey = applicationKey, user = user)

    private fun createMockLogBatchDTO(
        user: User,
        application: Application,
        tag: String = "default",
        format: LogFormats = LogFormats.UNKNOWN_FORMAT,
        logs: List<String> = listOf("Hello World!")
    ) = LogBatchDTO(user, application, tag, format, logs)

    private fun createCreateLogsReceiptCommand(
        application: Application,
        logsCount: Int = 1,
        source: String = LogDataSources.REST_BATCH.source
    ) = CreateLogsReceiptCommand(
        logsCount = logsCount,
        source = source,
        application = application
    )

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
        format: String = LogFormats.UNKNOWN_FORMAT.name,
        tag: String = "default",
        orderCounter: Long = 1,
        logs: List<String> = listOf("Hello World!")
    ) = logs.map { log ->
        Log(appName, appId, userKey, format, tag, orderCounter, log)
    }
}
