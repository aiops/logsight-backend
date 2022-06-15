package ai.logsight.backend.incidents.controller

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.TestInputConfig.incident
import ai.logsight.backend.TestInputConfig.incidentDTO
import ai.logsight.backend.TestInputConfig.incidentGroupDTO
import ai.logsight.backend.TestInputConfig.incidentId
import ai.logsight.backend.TestInputConfig.incidentMessage
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchException
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.incidents.domain.dto.IncidentGroupDTO
import ai.logsight.backend.incidents.extensions.toIncident
import ai.logsight.backend.incidents.extensions.toIncidentDTO
import ai.logsight.backend.incidents.extensions.toIncidentGroupDTO
import ai.logsight.backend.incidents.extensions.toIncidentMessageDTO
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.IncidentStorageService
import ai.logsight.backend.incidents.ports.web.request.GetGroupedIncidentsRequest
import ai.logsight.backend.incidents.ports.web.request.GetIncidentsRequest
import ai.logsight.backend.incidents.ports.web.request.UpdateIncidentRequest
import ai.logsight.backend.incidents.ports.web.response.*
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.*
import org.mockito.Mockito
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
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
internal class IncidentControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var incidentStorageService: IncidentStorageService

    companion object {
        const val endpoint = "/api/v1/logs/incidents"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        val updateIncidentRequest = UpdateIncidentRequest(incidentDTO)
    }

    @Nested
    @DisplayName("GET $endpoint/{incidentId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class GetIncidentById {
        private val getIncidentByIdEndpoint = "$endpoint/$incidentId"

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
        fun `should return an incident successfully`() {
            // given
            Mockito.`when`(incidentStorageService.findIncidentById(any())).thenReturn(incident)
            // when
            val result = mockMvc.get(getIncidentByIdEndpoint) // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(mapper.writeValueAsString(GetIncidentByIdResponse(incidentDTO)))
                }
            }
        }
    }

    @Nested
    @DisplayName("POST $endpoint")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class GetIncidentInTimeRange {
        private val getIncidentInTimeRangeEndpoint = "$endpoint/"

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
        fun `should return grouped incidents successfully for one group`() {
            // given
            Mockito.`when`(incidentStorageService.findIncidentsInTimeRange(any()))
                .thenReturn(listOf(incident))
            val request = GetIncidentsRequest(
                startTime = "2021-03-23T01:02:51.00700",
                stopTime = "2021-03-23T01:03:51.00700"
            )
            // when
            val result = mockMvc.post(getIncidentInTimeRangeEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            GetIncidentsResponse(listOf(incidentDTO))
                        )
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("POST $endpoint/grouped")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class GetGroupedIncidentInTimeRange {
        private val getGroupedIncidentInTimeRangeEndpoint = "$endpoint/grouped"

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
        fun `should return grouped incidents successfully for one group`() {
            // given
            Mockito.`when`(incidentStorageService.findIncidentsInTimeRange(any()))
                .thenReturn(List(TestInputConfig.incidentGroupSize) { incident })
            val request = GetGroupedIncidentsRequest(
                startTime = "2021-03-23T01:02:51.00700",
                stopTime = "2021-03-23T01:03:51.00700"
            )
            // when
            val result = mockMvc.post(getGroupedIncidentInTimeRangeEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            GetGroupedIncidentsResponse(listOf(incidentGroupDTO))
                        )
                    )
                }
            }
        }

        @Test
        fun `should return grouped incidents successfully for two groups`() {
            // given
            val incidentMessageAlt = incidentMessage.copy(template = "template1")
            val incidentDTOAlt = incidentDTO.copy(message = incidentMessageAlt.toIncidentMessageDTO())
            val incidentGroupDTOAlt = IncidentGroupDTO(
                head = incidentDTOAlt,
                incidents = List(TestInputConfig.incidentGroupSize) { incidentDTOAlt }
            )
            Mockito.`when`(incidentStorageService.findIncidentsInTimeRange(any()))
                .thenReturn(
                    List(TestInputConfig.incidentGroupSize) { incident } +
                        List(TestInputConfig.incidentGroupSize) { incidentDTOAlt.toIncident() }
                )
            val request = GetGroupedIncidentsRequest(
                startTime = "2021-03-23T01:02:51.00700",
                stopTime = "2021-03-23T01:03:51.00700"
            )
            // when
            val result = mockMvc.post(getGroupedIncidentInTimeRangeEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(request)
                accept = MediaType.APPLICATION_JSON
            }

            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            GetGroupedIncidentsResponse(listOf(incidentGroupDTO, incidentGroupDTOAlt))
                        )
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("DELETE $endpoint/{incidentId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class DeleteIncidentById {
        private val deleteIncidentByIdEndpoint = "$endpoint/$incidentId"

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
        fun `should delete a incident by ID successfully`() {
            // given
            Mockito.`when`(incidentStorageService.deleteIncidentById(any())).thenReturn(incidentId)
            // when
            val result = mockMvc.delete(deleteIncidentByIdEndpoint) // then
            result.andExpect {
                status { isNoContent() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(mapper.writeValueAsString(DeleteIncidentByIdResponse(incidentId)))
                }
            }
        }

        @Test
        fun `should throw elasticsearch exception when the entry cannot be deleted`() {
            // given
            Mockito.`when`(incidentStorageService.deleteIncidentById(any()))
                .thenThrow(ElasticsearchException::class.java)
            // when
            val result = mockMvc.delete(deleteIncidentByIdEndpoint) // then
            result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        }
    }

    @Nested
    @DisplayName("PUT $endpoint/{incidentId}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class UpdateIncidentById {
        private val updateIncidentByIdEndpoint = "$endpoint/$incidentId"

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
        fun `should update an incident by ID successfully`() {
            // given
            Mockito.`when`(incidentStorageService.findIncidentById(any())).thenReturn(incident)
            Mockito.`when`(incidentStorageService.updateIncident(any())).thenReturn(incident)
            // when
            val result = mockMvc.put(updateIncidentByIdEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateIncidentRequest)
                accept = MediaType.APPLICATION_JSON
            } // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(mapper.writeValueAsString(UpdateIncidentResponse(incident.toIncidentDTO())))
                }
            }
        }

        @Test
        fun `should throw elasticsearch exception when the entry cannot be deleted`() {
            // given
            Mockito.`when`(incidentStorageService.findIncidentById(any())).thenReturn(incident)
            Mockito.`when`(incidentStorageService.updateIncident(any())).thenThrow(ElasticsearchException::class.java)
            // when
            val result = mockMvc.put(updateIncidentByIdEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateIncidentRequest)
                accept = MediaType.APPLICATION_JSON
            } // then
            result.andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        }
    }
}
