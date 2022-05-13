package ai.logsight.backend.logs.ingestion.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.TestInputConfig.baseApp
import ai.logsight.backend.TestInputConfig.baseUser
import ai.logsight.backend.TestInputConfig.defaultTag
import ai.logsight.backend.TestInputConfig.logBatch
import ai.logsight.backend.TestInputConfig.logEvent
import ai.logsight.backend.TestInputConfig.logReceipt
import ai.logsight.backend.TestInputConfig.sendLogMessage
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.validation.Validation
import javax.validation.Validator

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
internal class LogsControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userStorageService: UserStorageService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @MockBean
    private lateinit var applicationStorageService: ApplicationStorageService

    @MockBean
    private lateinit var logsService: LogIngestionService

    @Nested
    @DisplayName("POST /api/v1/logs")
    @WithMockUser(username = TestInputConfig.baseEmail)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostLogs {

        @BeforeAll
        fun setUp() {
            applicationRepository.deleteAll()
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
            applicationRepository.save(TestInputConfig.baseAppEntityReady)
        }

        private val logsUriPath = "/api/v1/logs"

        private val mapper = ObjectMapper().registerModule(KotlinModule())!!

        private val defaultBody = SendLogListRequest(applicationId = baseApp.id, logs = listOf(logEvent))

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenReturn(baseApp)
            Mockito.`when`(logsService.processLogBatch(any()))
                .thenReturn(logReceipt)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isOk)
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.applicationId")
                        .value(baseApp.id.toString())
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.logsCount")
                        .value(logBatch.logs.size)
                )
        }

        @Test
        fun `should return not found because application is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenThrow(ApplicationNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isNotFound)
        }

        @Test
        fun `should return conflict because application is in invalid state`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenReturn(baseApp)
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
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenReturn(baseApp)
            Mockito.`when`(logsService.processLogBatch(any()))
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
    @DisplayName("POST /api/v1/logs/singles")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class PostLogsSingles {

        private val logsUriPath = "/api/v1/logs/singles"

        private val mapper = ObjectMapper().registerModule(KotlinModule())!!

        private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

        private val defaultBody = listOf(sendLogMessage)

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenReturn(baseApp)
            Mockito.`when`(logsService.processLogBatch(any()))
                .thenReturn(logReceipt)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isOk)
        }

        @Test
        fun `should return not found because user is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenThrow(UserNotFoundException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isNotFound)
        }

        @Test
        fun `should return conflict because application is in invalid state`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenReturn(baseApp)
            Mockito.`when`(logsService.processLogEvents(any()))
                .thenThrow(ApplicationStatusException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isConflict)
        }

        @Test
        fun `should return internal server error because application logs processing failed`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(applicationStorageService.findApplicationById(baseApp.id))
                .thenReturn(baseApp)
            Mockito.`when`(logsService.processLogEvents(any()))
                .thenThrow(RuntimeException::class.java)

            // when
            val result = performRequest()

            // then
            result.andExpect(status().isInternalServerError)
        }

        private fun getInvalidLogs(): List<Arguments> {
            return mapOf(
                "Empty timestamp" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "",
                    tags = defaultTag
                ),
                "Invalid time format" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876+02:00Z",
                    applicationId = baseApp.id,
                    tags = defaultTag
                ), // invalid email and not matching passwords
                "Invalid timestamp" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T12:",
                    applicationId = baseApp.id,
                    tags = defaultTag
                ),

            ).map { x -> Arguments.of(x.key, x.value) }
        }

        private fun getValidLogs(): List<Arguments> {
            return mapOf(
                "case 1" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = DateTime.now().toString(),
                    applicationId = baseApp.id,
                    tags = defaultTag
                ),
                "case 2" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876Z",
                    applicationId = baseApp.id,
                    tags = defaultTag
                ),
                "case 3" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14Z",
                    applicationId = baseApp.id,
                    tags = defaultTag
                ),
                "case 4" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876",
                    applicationId = baseApp.id,
                    tags = defaultTag
                ),
                "case 5" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876+02:00",
                    applicationId = baseApp.id,
                    tags = defaultTag
                ),

            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad request for {0}. ")
        @MethodSource("getInvalidLogs")
        fun `Bad request for invalid input`(
            reason: String,
            request: SendLogMessage
        ) {
            Assertions.assertFalse(validator.validate(request).isEmpty())
        }

        @ParameterizedTest(name = "Valid request for {0}. ")
        @MethodSource("getValidLogs")
        fun `Validate okay for valid input`(
            reason: String,
            request: SendLogMessage
        ) {
            Assertions.assertTrue(validator.validate(request).isEmpty())
        }

        private fun performRequest(requestBody: List<SendLogMessage> = defaultBody): ResultActions = mockMvc.perform(
            post(logsUriPath).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON)
        )
    }
}
