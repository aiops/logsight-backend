package ai.logsight.backend.compare.domain.service

import ai.logsight.backend.charts.domain.service.ESChartsServiceImpl
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsCompareAllDataPoint
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsCompareDataPoint
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.domain.dto.CompareDTO
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.compare.ports.out.HttpClientFactory
import ai.logsight.backend.compare.ports.out.config.CompareRESTConfigProperties
import ai.logsight.backend.compare.ports.web.response.CompareDataResponse
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class CompareService(
    private val restConfigProperties: CompareRESTConfigProperties,
    private val esChartsServiceImpl: ESChartsServiceImpl,
    private val elasticsearchService: ElasticsearchService,
    private val httpClientFactory: HttpClientFactory
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getCompareData(compareDTO: CompareDTO): CompareDataResponse {
        val uri = buildCompareEndpointURI()
        val requestBody = mapOf(
            CompareDTO::baselineTags.name to JSONObject(compareDTO.baselineTags).toString(),
            CompareDTO::candidateTags.name to JSONObject(compareDTO.candidateTags).toString(),
            CompareDTO::privateKey.name to compareDTO.privateKey,
        )
        val request = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody))).build()
        val response = httpClientFactory.create().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value()) throw RemoteCompareException(
            response.body().toString()
        )
        return mapper.readValue<CompareDataResponse>(response.body().toString())
    }

    fun getCompareByID(compareId: String?, user: User): List<HitsCompareDataPoint> {
        return esChartsServiceImpl.getCompareByID(compareId, user)
    }

    fun getAllCompares(user: User): List<HitsCompareAllDataPoint> {
        return esChartsServiceImpl.getAllCompares(user)
    }

    fun deleteCompareByID(compareId: String, user: User): String {
        return elasticsearchService.deleteByIndexAndDocID("${user.key}_verifications", compareId)
    }

    fun updateCompareStatusByID(compareId: String, compareStatus: Long, user: User): String {
        val parameters = hashMapOf("status" to (compareStatus as Any))
        return elasticsearchService.updateFieldsByIndexAndDocID(parameters, "${user.key}_verifications", compareId)
    }

    private fun buildCompareEndpointURI() =
        UriComponentsBuilder.newInstance().scheme(restConfigProperties.scheme).host(restConfigProperties.host)
            .port(restConfigProperties.port).path(restConfigProperties.comparePath).build().toUri()
}
