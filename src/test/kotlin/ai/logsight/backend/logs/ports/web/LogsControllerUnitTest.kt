package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogFormats
import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.LogDataSources
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.security.UserDetailsServiceImpl
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import ai.logsight.backend.users.ports.out.persistence.UserType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.*

@WithMockUser(username = "sasho@sasho.com")
@WebMvcTest(LogsController::class)
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
    private lateinit var logsService: LogsService

    @Nested
    @DisplayName("Post Logs")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostLogs {

        private val logsUriPath = "/api/v1/logs"

        private val email = "sasho@sasho.com"
        private val user = createMockUser(email)
        private val appId = UUID.randomUUID()
        private val app = createMockApplication(user = user, id = appId)
        private val log = "Hello World!"
        private val logBatchDTO = createMockLogBatchDTO(user, app, logs = listOf(log))
        private val orderCounter = 1L
        private val source = LogDataSources.REST_BATCH.source
        private val logsReceipt = createMockLogsReceipt(
            app, orderCounter = orderCounter, logsCount = logBatchDTO.logs.size, source = source
        )

        private val defaultBody = """{"applicationId": "$appId", "logs":["$log"]}"""

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogBatch(logBatchDTO))
                .thenReturn(logsReceipt)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.appId").value(appId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(orderCounter))
                .andExpect(MockMvcResultMatchers.jsonPath("$.logsCount").value(logBatchDTO.logs.size))
                .andExpect(MockMvcResultMatchers.jsonPath("$.source").value(source))
        }

        @Test
        fun `should return bad request because user is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenThrow(UserNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return bad request because application is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenThrow(ApplicationNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isBadRequest)
        }

        @Test
        fun `should return conflict because application is in invalid state`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(email))
                .thenReturn(user)
            Mockito.`when`(applicationStorageService.findApplicationById(appId))
                .thenReturn(app)
            Mockito.`when`(logsService.processLogBatch(logBatchDTO))
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

        private fun performRequest(requestBody: String = defaultBody): ResultActions =
            mockMvc.perform(
                post(logsUriPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .accept(MediaType.APPLICATION_JSON)
            )
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
        logFormat: LogFormats = LogFormats.UNKNOWN_FORMAT,
        logs: List<String> = listOf("Hello World!")
    ) = LogBatchDTO(user, application, tag, logFormat, logs)

    private fun createMockLogsReceipt(
        application: Application,
        id: UUID = UUID.randomUUID(),
        orderCounter: Long = 1,
        logsCount: Int = 1,
        source: String = "restBatch"
    ) = LogsReceipt(id, orderCounter, logsCount, source, application)
}
