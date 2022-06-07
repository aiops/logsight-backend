package ai.logsight.backend.logs.ingestion.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.TestInputConfig.baseUser
import ai.logsight.backend.TestInputConfig.defaultTag
import ai.logsight.backend.TestInputConfig.logBatch
import ai.logsight.backend.TestInputConfig.logEvent
import ai.logsight.backend.TestInputConfig.sendLogMessage
import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import ai.logsight.backend.logs.ingestion.ports.out.exceptions.LogSinkException
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.logs.ingestion.ports.web.responses.LogsReceiptResponse
import ai.logsight.backend.users.exceptions.UserNotFoundException
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import logCount.LogReceipt
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*
import javax.validation.Validation
import javax.validation.Validator

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles("test")
internal class LogsControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var logReceiptRepository: LogsReceiptRepository

    @MockBean
    private lateinit var logsReceiptStorageService: LogsReceiptStorageService

    @MockBean
    private lateinit var logSink: LogSink

    @MockBean
    private lateinit var userStorageService: UserStorageService

    private val logReceipt = LogReceipt(
        id = UUID.randomUUID(),
        logCount = logBatch.logs.size,
        batchId = logBatch.id,
        processedLogCount = 0,
        status = LogBatchStatus.PROCESSING
    )

    @Nested
    @DisplayName("POST /api/v1/logs")
    @WithMockUser(username = TestInputConfig.baseEmail)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostLogs {

        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            logReceiptRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @AfterAll
        fun tearDown() {
            userRepository.deleteAll()
            logReceiptRepository.deleteAll()
        }

        private val logsUriPath = "/api/v1/logs"

        private val mapper = ObjectMapper().registerModule(KotlinModule())!!

        private val defaultBody = SendLogListRequest(logs = listOf(logEvent))

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(logsReceiptStorageService.saveLogReceipt(any()))
                .thenReturn(logReceipt)

            val expectedResult = LogsReceiptResponse(
                logsCount = logReceipt.logCount,
                receiptId = logReceipt.id,
                processedLogsCount = logReceipt.processedLogCount,
                status = logReceipt.status,
                batchId = logReceipt.batchId
            )
            // when
            val result = mockMvc.post(logsUriPath) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(defaultBody)
                accept = MediaType.APPLICATION_JSON
            }

            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            expectedResult
                        )
                    )
                }
                // then
            }
        }

        @Test
        fun `should return internal server error because application logs processing failed`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(logSink.sendLogBatch(any()))
                .thenThrow(LogSinkException::class.java)
            Mockito.`when`(logsReceiptStorageService.saveLogReceipt(any()))
                .thenReturn(logReceipt)

            // when
            val result = mockMvc.post(logsUriPath) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(defaultBody)
                accept = MediaType.APPLICATION_JSON
            }

            val exception = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertTrue(exception is LogSinkException)

            // then
        }
    }

    @Nested
    @DisplayName("POST /api/v1/logs/singles")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class PostLogsSingles {

        private val logsUriPath = "/api/v1/logs/singles"

        private val mapper = ObjectMapper().registerModule(KotlinModule())!!

        private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(logsReceiptStorageService.saveLogReceipt(any()))
                .thenReturn(logReceipt)

            val expectedResult = LogsReceiptResponse(
                logsCount = logReceipt.logCount,
                receiptId = logReceipt.id,
                processedLogsCount = logReceipt.processedLogCount,
                status = logReceipt.status,
                batchId = logReceipt.batchId
            )
            // when
            val result = mockMvc.post(logsUriPath) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(listOf(sendLogMessage))
                accept = MediaType.APPLICATION_JSON
            }

            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            expectedResult
                        )
                    )
                }
                // then
            }
        }

        @Test
        fun `should return not found because user is not found`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenThrow(UserNotFoundException::class.java)

            // when
            val result = mockMvc.post(logsUriPath) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(listOf(sendLogMessage))
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun `should return internal server error because application logs processing failed`() {
            // given
            Mockito.`when`(userStorageService.findUserByEmail(baseUser.email))
                .thenReturn(baseUser)
            Mockito.`when`(logSink.sendLogBatch(any()))
                .thenThrow(LogSinkException::class.java)
            Mockito.`when`(logsReceiptStorageService.saveLogReceipt(any()))
                .thenReturn(logReceipt)
            // when
            val result = mockMvc.post(logsUriPath) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(listOf(sendLogMessage))
                accept = MediaType.APPLICATION_JSON
            }

            val exception = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            Assertions.assertTrue(exception is LogSinkException)
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
                    tags = defaultTag
                ),
                "Invalid timestamp" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T12:",
                    tags = defaultTag
                ),

            ).map { x -> Arguments.of(x.key, x.value) }
        }

        private fun getValidLogs(): List<Arguments> {
            return mapOf(
                "case 1" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = DateTime.now().toString(),
                    tags = defaultTag
                ),
                "case 2" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876Z",
                    tags = defaultTag
                ),
                "case 3" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14Z",
                    tags = defaultTag
                ),
                "case 4" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876",
                    tags = defaultTag
                ),
                "case 5" to SendLogMessage(
                    message = "Hello World!",
                    timestamp = "2016-05-24T15:54:14.876+02:00",
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
    }
}
