package ai.logsight.backend.tags.controller

import ai.logsight.backend.TestInputConfig
import ai.logsight.backend.charts.domain.service.ESChartsServiceImpl
import ai.logsight.backend.compare.controller.request.TagKeyResponse
import ai.logsight.backend.compare.controller.request.TagValueRequest
import ai.logsight.backend.compare.controller.request.TagValueResponse
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.compare.dto.TagKey
import ai.logsight.backend.compare.ports.web.request.GetCompareResultRequest
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.MethodArgumentNotValidException

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
internal class TagsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var esChartsServiceImpl: ESChartsServiceImpl

    companion object {
        const val endpoint = "/api/v1/logs/tags"
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        val tagValueRequest = TagValueRequest("tag", "default")
    }

    @Nested
    @DisplayName("POST $endpoint/values")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class GetCompareTagValues {
        private val getCompareTagEndpoint = "$endpoint/values"

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
        fun `should return list of values for a tag name`() {
            // given
            Mockito.`when`(esChartsServiceImpl.getCompareTagValues(any(), any(), any()))
                .thenReturn(listOf(Tag("tag", "default", 1)))
            // when
            val result = mockMvc.post(getCompareTagEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(tagValueRequest)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            TagValueResponse(listOf(Tag("tag", "default", 1)))
                        )
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("POST $endpoint/filter")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @WithMockUser(username = TestInputConfig.baseEmail)
    inner class GetCompareTags {
        private val getCompareTagsEndpoint = "$endpoint/filter"

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
        fun `should return list of tags for a given filter tags`() {
            // given
            Mockito.`when`(esChartsServiceImpl.getCompareTagFilter(any(), any(), any()))
                .thenReturn(listOf(TagKey("tag", 1)))
            // when
            val result = mockMvc.post(getCompareTagsEndpoint) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(tagValueRequest)
                accept = MediaType.APPLICATION_JSON
            }
            // then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    json(
                        mapper.writeValueAsString(
                            TagKeyResponse(listOf(TagKey("tag", 1)))
                        )
                    )
                }
            }
        }
    }
}
