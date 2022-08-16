package ai.logsight.backend.compare.ports.web

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.charts.domain.service.ESChartsServiceImpl
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsCompareDataPoint
import ai.logsight.backend.compare.ports.out.HttpClientFactory
import ai.logsight.backend.compare.ports.web.request.GetCompareResultRequest
import ai.logsight.backend.compare.ports.web.request.UpdateCompareStatusRequest
import ai.logsight.backend.compare.ports.web.response.CompareDataResponse
import ai.logsight.backend.compare.ports.web.response.DeleteCompareByIdResponse
import ai.logsight.backend.compare.ports.web.response.UpdateCompareStatusResponse
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchException
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.json.JSONArray
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.MethodArgumentNotValidException
import java.net.http.HttpClient
import java.net.http.HttpResponse

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
internal class CompareControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var esChartsServiceImpl: ESChartsServiceImpl

    @MockBean
    private lateinit var elasticsearchService: ElasticsearchService

    @MockBean
    private lateinit var httpClientFactory: HttpClientFactory

    companion object {
        const val endpoint = "/api/v1/logs/compare"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        var json = "{ \"f1\" : \"v1\" } "
        var objectMapper = ObjectMapper()
        var jsonNode = objectMapper.readTree(json)!!

        const val compareId = "exampleCompareId"

        val updateCompareStatusRequest = UpdateCompareStatusRequest(compareId, 1)

        val createCompareRequest =
            GetCompareResultRequest(baselineTags = mapOf("tag" to "default"), candidateTags = mapOf("tag" to "default"))
        val compareResponse = CompareDataResponse(
            link = "http://localhost:4200/pages/compare?compareId=compareId",
            baselineTags = mapOf("tag" to "default"),
            candidateTags = mapOf("tag" to "default"),
            compareId = "compareId",
            risk = 0,
            totalLogCount = 0,
            baselineLogCount = 0,
            candidateLogCount = 0,
            candidateChangePercentage = 0.0,
            addedStatesTotalCount = 0,
            addedStatesReportPercentage = 0.0,
            addedStatesFaultPercentage = 0.0,
            deletedStatesTotalCount = 0,
            deletedStatesReportPercentage = 0.0,
            deletedStatesFaultPercentage = 0.0,
            recurringStatesTotalCount = 0,
            recurringStatesReportPercentage = 0.0,
            recurringStatesFaultPercentage = 0.0,
            frequencyChangeTotalCount = 0,
            frequencyChangeReportPercentage = jsonNode,
            frequencyChangeFaultPercentage = jsonNode
        )
    }

    @Nested
    @DisplayName("POST $endpoint")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class CreateCompare {
        private val createEndpoint = endpoint

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @AfterAll
        fun teardown() {
            userRepository.deleteAll()
        }

        @Test
        fun `should create a new verification successfully`() {
            // given
            val httpClientMock = mock(HttpClient::class.java)
            val mockedResponse: HttpResponse<String> = mockk(relaxed = true)
            every { mockedResponse.statusCode() } returns 200
            every { mockedResponse.body() } returns mapper.writeValueAsString(compareResponse)
//            Mockito.`when`(mockedResponse.statusCode()).thenReturn(200)
//            Mockito.`when`(mockedResponse.body()).thenReturn("ok")

            Mockito.`when`(httpClientFactory.create()).thenReturn(httpClientMock)
            Mockito.`when`(httpClientMock.send(any(), any<HttpResponse.BodyHandler<String>>()))
                .thenReturn(mockedResponse)
            // when
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(createCompareRequest)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            compareResponse
                        )
                    )
                }
            }
        }

        private fun getInvalidRequests(): List<Arguments> {
            return mapOf(
                "Empty baseline tags" to GetCompareResultRequest(
                    baselineTags = mapOf(), candidateTags = mapOf("tag" to "default")
                ),
                "Empty candidate tags" to GetCompareResultRequest(
                    baselineTags = mapOf(), candidateTags = mapOf("tag" to "default")
                ),
            ).map { x -> Arguments.of(x.key, x.value) }
        }

        @ParameterizedTest(name = "Bad request for {0}. ")
        @MethodSource("getInvalidRequests")
        fun `Bad request for invalid input`(
            reason: String,
            request: GetCompareResultRequest
        ) {

            // given
            // when
            val result = mockMvc.post(createEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            val exception = result.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn().resolvedException
            Assertions.assertThat(exception is MethodArgumentNotValidException)
        }
    }

    @Nested
    @DisplayName("GET $endpoint/{compareId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class GetCompareById {
        private val getCompareByIdEndpoint = "$endpoint/$compareId"

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @AfterAll
        fun teardown() {
            userRepository.deleteAll()
        }

        @Test
        fun `should return a list of compares successfully`() {
            // given
            Mockito.`when`(esChartsServiceImpl.getCompareByID(any(), any())).thenReturn(
                listOf(HitsCompareDataPoint(compareId = compareId, source = jsonNode))
            )
            // when
            val result = mockMvc.get(getCompareByIdEndpoint) // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    JSONArray(
                        "[{\"_id\":\"exampleCompareId\",\"_source\":{\"f1\":\"v1\"}}]"
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("DELETE $endpoint/{compareId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class DeleteCompareById {
        private val deleteCompareByIdEndpoint = "$endpoint/$compareId"

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @AfterAll
        fun teardown() {
            userRepository.deleteAll()
        }

        @Test
        fun `should delete a compare by ID successfully`() {
            // given
            Mockito.`when`(elasticsearchService.deleteByIndexAndDocID(any(), any())).thenReturn(compareId)
            // when
            val result = mockMvc.delete(deleteCompareByIdEndpoint) // then
            result.andExpect {
                status { isNoContent() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(mapper.writeValueAsString(DeleteCompareByIdResponse(compareId)))
                }
            }
        }

        @Test
        fun `should throw elasticsearch exception when the entry cannot be deleted`() {
            // given
            Mockito.`when`(elasticsearchService.deleteByIndexAndDocID(any(), any()))
                .thenThrow(ElasticsearchException::class.java)
            // when
            val result = mockMvc.delete(deleteCompareByIdEndpoint) // then
            result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        }
    }

    @Nested
    @DisplayName("POST $endpoint/status")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class UpdateCompareById {
        private val updateCompareByIdEndpoint = "$endpoint/status"

        @BeforeAll
        fun setup() {
            userRepository.deleteAll()
            userRepository.save(TestInputConfig.baseUserEntity)
        }

        @AfterAll
        fun teardown() {
            userRepository.deleteAll()
        }

        @Test
        fun `should update a compare by ID successfully`() {
            // given
            Mockito.`when`(elasticsearchService.updateFieldsByIndexAndDocID(any(), any(), any())).thenReturn(compareId)
            // when
            val result = mockMvc.post(updateCompareByIdEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateCompareStatusRequest)
                accept = MediaType.APPLICATION_JSON
            } // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(mapper.writeValueAsString(UpdateCompareStatusResponse(compareId)))
                }
            }
        }

        @Test
        fun `should throw elasticsearch exception when the entry cannot be deleted`() {
            // given
            Mockito.`when`(elasticsearchService.updateFieldsByIndexAndDocID(any(), any(), any()))
                .thenThrow(ElasticsearchException::class.java)
            // when
            val result = mockMvc.delete(updateCompareByIdEndpoint) // then
            result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        }
    }
}
