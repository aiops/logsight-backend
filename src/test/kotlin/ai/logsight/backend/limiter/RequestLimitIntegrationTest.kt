package ai.logsight.backend.limiter

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService

import ai.logsight.backend.users.ports.out.persistence.FindUserServiceImpl
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(LogIngestionController::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RequestLimitIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var logsService: LogIngestionService

    @MockBean
    private lateinit var findUserService: FindUserServiceImpl

    @Autowired
    private lateinit var userRepository: UserRepository

    @WithMockUser(username = TestInputConfig.baseEmail)
    @Nested
    @DisplayName("POST /api/v1/logs/singles")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostLogs {

        @BeforeAll
        fun setUp() {
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @AfterAll
        fun tearDown() {
            userRepository.deleteAll()
        }

        private val logsUriPath = "/api/v1/logs/singles"

        private val mapper = ObjectMapper().registerModule(KotlinModule())!!

        private val defaultBody = listOf(TestInputConfig.sendLogMessage)

        @Test
        fun `should return valid log receipt response`() {
            // given
            Mockito.`when`(findUserService.findUserByEmail(TestInputConfig.baseUser.email))
                .thenReturn(TestInputConfig.baseUser)
            Mockito.`when`(logsService.processLogBatch(any())).thenReturn(TestInputConfig.logReceipt)
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
                            TestInputConfig.logReceiptResponse
                        )
                    )
                }
                // then
            }
        }
    }
}