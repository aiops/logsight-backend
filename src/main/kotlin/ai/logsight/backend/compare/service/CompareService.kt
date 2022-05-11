package ai.logsight.backend.compare.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.response.CompareDataResponse
import ai.logsight.backend.compare.dto.CompareDTO
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.compare.out.rest.config.CompareRESTConfigProperties
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.flush.exceptions.FlushAlreadyPendingException
import ai.logsight.backend.flush.ports.persistence.FlushStorageService
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
import java.util.*

@Service
class CompareService(
    private val restConfigProperties: CompareRESTConfigProperties,
    private val applicationStorageService: ApplicationStorageService,
    private val chartsRepository: ESChartRepository,
    private val flushStorageService: FlushStorageService,
    private val chartsService: ChartsService
) {
    val resetTemplate = RestTemplateConnector()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getCompareDataView(compareDTO: CompareDTO): String {
        val uri = buildCompareEndpointURI(compareDTO)
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .GET()
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

        val uri = buildCompareEndpointURI(compareDTO)
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpStatus.OK.value())
            throw RemoteCompareException(
                response.body()
                    .toString()
            )
        return mapper.readValue<CompareDataResponse>(response.body().toString())
    }

    fun getCompareTags(userId: UUID, applicationId: UUID): MutableList<Tag> {
        val application = applicationStorageService.findApplicationById(applicationId)
        val applicationIndex = "${application.user.key}_${application.name}_log_ad"
        val chartRequest = ChartRequest(applicationId = applicationId, chartConfig = ChartConfig("util", "now", "now", "versions", "log_ad"))
        val getChartDataQuery = chartsService.getChartQuery(application.user.id, chartRequest)
        val tags = chartsRepository.getData(getChartDataQuery, applicationIndex) // use mapper here
        val dataList = mutableListOf<Tag>()
        JSONObject(tags)
            .getJSONObject("aggregations")
            .getJSONObject("listAggregations")
            .getJSONArray("buckets")
            .forEach {
                dataList.add(Tag(JSONObject(it.toString()).getString("key"), JSONObject(it.toString()).getString("key")))
            }
        return dataList
    }

    private fun buildCompareEndpointURI(compareDTO: CompareDTO) =
        UriComponentsBuilder.newInstance()
            .scheme(restConfigProperties.scheme)
            .host(restConfigProperties.host)
            .port(restConfigProperties.port)
            .path(restConfigProperties.comparePath)
            .queryParam(CompareDTO::applicationId.name, compareDTO.applicationId)
            .queryParam(CompareDTO::applicationName.name, compareDTO.applicationName)
            .queryParam(CompareDTO::baselineTag.name, compareDTO.baselineTag)
            .queryParam(CompareDTO::compareTag.name, compareDTO.compareTag)
            .queryParam(CompareDTO::privateKey.name, compareDTO.privateKey)
            .build()
            .toUri()
}
