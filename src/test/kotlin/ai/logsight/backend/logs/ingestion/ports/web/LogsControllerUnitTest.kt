package ai.logsight.backend.logs.ingestion.ports.web

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.security.UserDetailsServiceImpl
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import ai.logsight.backend.users.ports.out.persistence.UserType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.*
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@WithMockUser(username = "sasho@sasho.com")
@WebMvcTest(LogsController::class)
@DirtiesContext
internal class LogsControllerUnitTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userDetailsServiceImpl: UserDetailsServiceImpl

    @MockBean
    private lateinit var userStorageService: UserStorageService

    @MockBean
    private lateinit var applicationStorageService: ApplicationStorageService

    @MockBean
    private lateinit var logsService: LogIngestionService

    @Nested
    @DisplayName("Post Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostLogs {

        private val logsUriPath = "/api/v1/logs"

        val mapper = ObjectMapper().registerModule(KotlinModule())!!

        private val email = "sasho@sasho.com"
        private val user = createMockUser(email)
        private val appId = UUID.randomUUID()
        private val app = createMockApplication(user = user, id = appId)
        private val log = LogMessage(
            message = "Hello World!",
            timestamp = DateTime.now()
                .toString()
        )

        private val logBatchDTO = createMockLogBatchDTO(user, app, logs = listOf(log))
        private val orderCounter = 1L
        private val source = LogDataSources.REST_BATCH.source
        private val logsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logBatchDTO.logs.size, source = source
        )

        private val defaultBody = SendLogListRequest(applicationId = appId, logs = listOf(log))

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogBatch(any()))
                .thenReturn(logsReceipt)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isOk)
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.applicationId")
                        .value(appId.toString())
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.logsCount")
                        .value(logBatchDTO.logs.size)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.source")
                        .value(source)
                )
        }

        @Test
        fun `should return not found because user is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenThrow(UserNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isNotFound)
        }

        @Test
        fun `should return not found because application is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenThrow(ApplicationNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isNotFound)
        }

        @Test
        fun `should return conflict because application is in invalid state`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogBatch(any()))
                .thenThrow(ApplicationStatusException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isConflict)
        }

        @Test
        fun `should return internal server error because application logs processing failed`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogBatch(logBatchDTO))
                .thenThrow(RuntimeException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isInternalServerError)
        }

        private fun performRequest(requestBody: SendLogListRequest = defaultBody): ResultActions = mockMvc.perform(
            post(logsUriPath).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    @Nested
    @DisplayName("Post Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostLogsSingles {

        private val logsUriPath = "/api/v1/logs/singles"

        private val mapper = ObjectMapper().registerModule(KotlinModule())!!

        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        private val validator: Validator = factory.validator

        private val email = "sasho@sasho.com"
        private val user = createMockUser(email)
        private val appId = UUID.randomUUID()
        private val app = createMockApplication(user = user, id = appId)
        private val log = SendLogMessage(
            message = "Hello World!",
            timestamp = DateTime.now()
                .toString(),
            applicationId = app.id,
            tag = "default"
        )

        private val logBatchDTO = createMockLogBatchDTO(
            user,
            app,
            logs = listOf(log).map { LogMessage(timestamp = it.timestamp, message = it.message) }
        )

        private val orderCounter = 1L
        private val source = LogDataSources.REST_BATCH.source
        private val logsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logBatchDTO.logs.size, source = source
        )

        private val defaultBody = listOf(log)

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogBatch(any()))
                .thenReturn(logsReceipt)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isOk)
        }

        @Test
        fun `should return not found because user is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenThrow(UserNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isNotFound)
        }

        @Test
        fun `should return conflict because application is in invalid state`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogSingles(any()))
                .thenThrow(ApplicationStatusException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isConflict)
        }

        @Test
        fun `should return internal server error because application logs processing failed`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogSingles(any()))
                .thenThrow(RuntimeException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isInternalServerError)
        }

        @Test
        fun `should check validity in SendLogMessage`() {
            // given
            val logsPass = listOf(
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = DateTime.now().toString(),
                    applicationId = app.id,
                    tag = "default"
                ),
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876Z",
                    applicationId = app.id,
                    tag = "default"
                ),
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14Z",
                    applicationId = app.id,
                    tag = "default"
                ),
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876",
                    applicationId = app.id,
                    tag = "default"
                ),
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876+02:00",
                    applicationId = app.id,
                    tag = "default"
                ),
            )
            val logsFail = listOf(
                SendLogMessage(message = "Hello World!", timestamp = DateTime.now().toString(), tag = "default"),
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876+02:00Z",
                    applicationId = app.id,
                    tag = "default"
                ),
                SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T12:",
                    applicationId = app.id,
                    tag = "default"
                )
            )

            // when
            val violationsPass = mutableListOf<Set<ConstraintViolation<SendLogMessage>>>()
            logsPass.forEach { log ->
                run {
                    violationsPass.add(validator.validate(log))
                }
            }
            val violationsFail = mutableListOf<Set<ConstraintViolation<SendLogMessage>>>()
            logsFail.forEach { log ->
                run {
                    violationsFail.add(validator.validate(log))
                }
            }

            // then
            violationsPass.forEach { violation ->
                run {
                    Assertions.assertTrue(violation.isEmpty())
                }
            }
            violationsFail.forEach { violation ->
                run {
                    Assertions.assertFalse(violation.isEmpty())
                }
            }
        }

        private fun performRequest(requestBody: List<SendLogMessage> = defaultBody): ResultActions = mockMvc.perform(
            post(logsUriPath).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    private fun createMockUser(email: String) = User(
        id = UUID.randomUUID(),
        email = email,
        password = "sasho",
        key = "key",
        dateCreated = LocalDateTime.now(),
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
        logs: List<LogMessage> = listOf(
            LogMessage(
                timestamp = DateTime.now()
                    .toString(),
                message = "Hello World!"
            )
        )
    ) = LogBatchDTO(user, application, tag, logs, LogDataSources.SAMPLE)

    private fun createMockLogsReceipt(
        application: Application,
        id: UUID = UUID.randomUUID(),
        orderCounter: Long = 1,
        logsCount: Int = 1,
        source: String = "restBatch"
    ) = LogsReceipt(id, orderCounter, logsCount, source, application)
}
