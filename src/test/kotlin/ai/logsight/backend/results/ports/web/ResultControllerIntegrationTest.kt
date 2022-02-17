package ai.logsight.backend.results.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.rpc.RPCService
import ai.logsight.backend.logs.domain.service.LogDataSources
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.extensions.toLogsReceiptEntity
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.results.domain.ResultOperations
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.exceptions.ResultInitAlreadyPendingException
import ai.logsight.backend.results.ports.persistence.ResultInitEntity
import ai.logsight.backend.results.ports.persistence.ResultInitRepository
import ai.logsight.backend.results.ports.rpc.ResultInitRPCService
import ai.logsight.backend.results.ports.web.request.CreateResultInitRequest
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
internal class ResultControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var receiptStorageService: LogsReceiptStorageService

    @Autowired
    private lateinit var appRepository: ApplicationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var receiptRepository: LogsReceiptRepository

    @Autowired
    private lateinit var resultInitRepository: ResultInitRepository

    @MockBean
    private lateinit var resultInitRPCService: ResultInitRPCService

    @MockBean
    @Qualifier("ZeroMQ")
    private lateinit var analyticsManagerAppRPC: RPCService

    companion object {
        const val endpoint = "/api/v1/results"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
    }

    @Nested
    @DisplayName("POST $endpoint")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class CreateResultInit {
        @BeforeEach
        fun setup() {
            userRepository.deleteAll()
            appRepository.deleteAll()
            receiptRepository.deleteAll()
        }

        @Test
        fun `should create result init successfully`() {
            // given
            val user = userRepository.save(TestInputConfig.baseUserEntity)
            val application = appRepository.save(TestInputConfig.baseAppEntity)
            val receipt = receiptStorageService.saveLogsReceipt(
                CreateLogsReceiptCommand(
                    2000,
                    LogDataSources.REST_BATCH.source,
                    application.toApplication()
                )
            )
            val request = CreateResultInitRequest(receiptId = receipt.id, ResultOperations.INIT)
            // when
            val result = mockMvc.post("/api/v1/results") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                assert(ResultInitStatus.PENDING.name in result.andReturn().response.contentAsString)
            }
                .andReturn().response.contentAsString
        }

        @Test
        fun `should return error when result init receipt id does not exist`() {
            // given
            val user = userRepository.save(TestInputConfig.baseUserEntity)
            val application = appRepository.save(TestInputConfig.baseAppEntity)
            val request = CreateResultInitRequest(receiptId = application.id, ResultOperations.INIT) // wrong id
            // when
            val result = mockMvc.post("/api/v1/results") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().response.contentAsString
        }

        @Test
        fun `should return exception if there is an already pending receipt for the same application`() {
            // given
            userRepository.save(TestInputConfig.baseUserEntity)
            val application = appRepository.save(TestInputConfig.baseAppEntity)
            val receipt = receiptStorageService.saveLogsReceipt(
                CreateLogsReceiptCommand(
                    2000,
                    LogDataSources.REST_BATCH.source,
                    application.toApplication()
                )
            )
            val request = CreateResultInitRequest(receiptId = receipt.id, ResultOperations.INIT)
            resultInitRepository.save(
                ResultInitEntity(
                    UUID.randomUUID(),
                    ResultInitStatus.PENDING,
                    receipt.toLogsReceiptEntity()
                )
            )
            // when
            val result = mockMvc.post("/api/v1/results") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isConflict() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            org.assertj.core.api.Assertions.assertThat(exception is ResultInitAlreadyPendingException)
        }

        @Test
        fun `should return exception and delete the result init entry in db if RPC service fails`() {
            // given
            userRepository.save(TestInputConfig.baseUserEntity)
            val application = appRepository.save(TestInputConfig.baseAppEntity)
            val receipt = receiptStorageService.saveLogsReceipt(
                CreateLogsReceiptCommand(
                    2000,
                    LogDataSources.REST_BATCH.source,
                    application.toApplication()
                )
            )
            val request = CreateResultInitRequest(receiptId = receipt.id, ResultOperations.INIT)
            Mockito.`when`(resultInitRPCService.flush(any(), any()))
                .thenThrow(RuntimeException::class.java)
            // when
            val result = mockMvc.post("/api/v1/results") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
                assert(resultInitRepository.count() == 0L) // no entries should be there
            }
                .andReturn().resolvedException
            org.assertj.core.api.Assertions.assertThat(exception is RuntimeException)
        }
    }
}
