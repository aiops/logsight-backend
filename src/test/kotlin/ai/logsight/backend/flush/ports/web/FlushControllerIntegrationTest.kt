package ai.logsight.backend.flush.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.application.ports.out.rpc.RPCService
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceiptEntity
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptRepository
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.flush.exceptions.FlushAlreadyPendingException
import ai.logsight.backend.flush.ports.persistence.FlushEntity
import ai.logsight.backend.flush.ports.persistence.FlushRepository
import ai.logsight.backend.flush.ports.rpc.FlushRPCService
import ai.logsight.backend.flush.ports.web.request.CreateFlushRequest
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.*
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
internal class FlushControllerIntegrationTest {

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
    private lateinit var flushRepository: FlushRepository

    @MockBean
    private lateinit var flushRPCService: FlushRPCService

    @MockBean
    @Qualifier("ZeroMQ")
    private lateinit var analyticsManagerAppRPC: RPCService

    companion object {
        const val endpoint = "/api/v1/logs/flush"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
    }

    @Nested
    @DisplayName("POST $endpoint")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class CreateFlush {
        @BeforeEach
        fun setup() {
            userRepository.deleteAll()
            appRepository.deleteAll()
            receiptRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @Test
        fun `should create result init successfully`() {
            // given
            val application = appRepository.save(TestInputConfig.baseAppEntity)
            val receipt = receiptStorageService.saveLogsReceipt(
                CreateLogsReceiptCommand(
                    2000,
                    LogDataSources.REST_BATCH.source,
                    application.toApplication()
                )
            )
            val request = CreateFlushRequest(receiptId = receipt.id)
            // when
            val result = mockMvc.post(endpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                assert(FlushStatus.PENDING.name in result.andReturn().response.contentAsString)
            }
                .andReturn().response.contentAsString
        }

        @Test
        fun `should return error when result init receipt id does not exist`() {
            // given
            val application = appRepository.save(TestInputConfig.baseAppEntity)
            val request = CreateFlushRequest(receiptId = application.id) // wrong id
            // when
            val result = mockMvc.post(endpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().response.contentAsString
        }

        @Test
        fun `should return valid if there is an already pending receipt for the same application`() {
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
            val request = CreateFlushRequest(receiptId = receipt.id)
            flushRepository.save(
                FlushEntity(
                    UUID.randomUUID(),
                    FlushStatus.PENDING,
                    receipt.toLogsReceiptEntity()
                )
            )
            // when
            val result = mockMvc.post(endpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
                .andReturn().resolvedException
            org.assertj.core.api.Assertions.assertThat(exception is FlushAlreadyPendingException)
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
            val request = CreateFlushRequest(receiptId = receipt.id)
            Mockito.`when`(flushRPCService.flush(any(), any()))
                .thenThrow(RuntimeException::class.java)
            // when
            val result = mockMvc.post(endpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
                assert(flushRepository.count() == 0L) // no entries should be there
            }
                .andReturn().resolvedException
            org.assertj.core.api.Assertions.assertThat(exception is RuntimeException)
        }
    }
}
