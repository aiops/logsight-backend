package ai.logsight.backend.compare.service

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.TagData
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.compare.controller.response.CompareDataResponse
import ai.logsight.backend.compare.dto.CompareDTO
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.compare.out.rest.config.CompareRESTConfigProperties
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.flush.exceptions.FlushAlreadyPendingException
import ai.logsight.backend.flush.ports.persistence.FlushStorageService
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class CompareService(
    private val restConfigProperties: CompareRESTConfigProperties,
    private val flushStorageService: FlushStorageService,
) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    // this contains also the table with the states/templates
    fun getCompareDataView(compareDTO: CompareDTO): String {
        val uri = buildCompareEndpointURI()
        val requestBody = mapOf(
            CompareDTO::applicationId.name to compareDTO.applicationId,
            CompareDTO::applicationName.name to compareDTO.applicationName,
            CompareDTO::baselineTags.name to mapper.writeValueAsString(compareDTO.baselineTags),
            CompareDTO::compareTags.name to mapper.writeValueAsString(compareDTO.compareTags),
            CompareDTO::privateKey.name to compareDTO.privateKey
        )
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value())
            throw RemoteCompareException(
                response.body()
                    .toString()
            )
        return response.body().toString()
    }

    fun getCompareData(compareDTO: CompareDTO): CompareDataResponse {
        val flush = compareDTO.flushId?.let {
            flushStorageService.findFlushById(it)
        }
        if (flush != null && flush.status != FlushStatus.DONE) {
            throw FlushAlreadyPendingException("Flush is not yet finished. Please retry. Send a request without a flushId to ignore the flush request and get the results immediately.")
        }
        return mapper.readValue<CompareDataResponse>(getCompareDataView(compareDTO))
    }

    private fun buildCompareEndpointURI() =
        UriComponentsBuilder.newInstance()
            .scheme(restConfigProperties.scheme)
            .host(restConfigProperties.host)
            .port(restConfigProperties.port)
            .path(restConfigProperties.comparePath)
            .build()
            .toUri()
}
