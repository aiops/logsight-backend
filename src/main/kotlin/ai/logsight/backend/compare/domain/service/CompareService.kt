package ai.logsight.backend.compare.domain.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.domain.dto.CompareDTO
import ai.logsight.backend.compare.domain.dto.Tag
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.compare.ports.out.config.CompareRESTConfigProperties
import ai.logsight.backend.compare.ports.web.response.CompareDataResponse
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
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
    private val logsReceiptStorageService: LogsReceiptStorageService,
    private val chartsRepository: ESChartRepository,
    private val chartsService: ChartsService
) {
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
        val logsReceipt = compareDTO.logsReceiptId?.let {
            logsReceiptStorageService.findLogsReceiptById(it)
        }

        // TODO : Check if logs with logs receipt ID were already written to elasticsearch

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
        return mapper.readValue(response.body().toString())
    }

    fun getCompareTags(userId: UUID, applicationId: UUID): MutableList<Tag> {
        val application = applicationStorageService.findApplicationById(applicationId)
        val applicationIndex = "${application.user.key}_${application.name}_log_ad"
        val chartRequest = ChartRequest(
            applicationId = applicationId,
            chartConfig = ChartConfig("util", "now", "now", "versions", "log_ad")
        )
        val getChartDataQuery = chartsService.getChartQuery(application.user.id, chartRequest)
        val tags = chartsRepository.getData(getChartDataQuery, applicationIndex) // use mapper here
        val dataList = mutableListOf<Tag>()
        JSONObject(tags)
            .getJSONObject("aggregations")
            .getJSONObject("listAggregations")
            .getJSONArray("buckets")
            .forEach {
                dataList.add(
                    Tag(
                        JSONObject(it.toString()).getString("key"),
                        JSONObject(it.toString()).getString("key")
                    )
                )
            }
        return dataList
    }

    // TODO move REST logic to REST service, use REST connector there

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
