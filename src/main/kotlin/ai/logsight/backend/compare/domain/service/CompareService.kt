package ai.logsight.backend.compare.domain.service

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsCompareDataPoint
import ai.logsight.backend.charts.repository.entities.elasticsearch.TableCompare
import ai.logsight.backend.charts.repository.entities.elasticsearch.VerticalBarBucket
import ai.logsight.backend.charts.repository.entities.elasticsearch.VerticalBarChartData
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.compare.domain.dto.CompareDTO
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.compare.ports.out.config.CompareRESTConfigProperties
import ai.logsight.backend.compare.ports.web.response.CompareDataResponse
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
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
    private val logsReceiptStorageService: LogsReceiptStorageService,
    private val chartsService: ChartsService,
    private val chartsRepository: ESChartRepository,
    private val elasticsearchService: ElasticsearchService
) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getBaselineTagsQuery(baselineTags: Map<String, String>): String {
        val filterQuery = mutableListOf<JSONObject>()
        baselineTags.forEach {
            filterQuery.add(JSONObject(mapOf("match_phrase" to JSONObject(mapOf("baseline_tags.${it.key}.keyword" to it.value)))))
        }
        val result = filterQuery.toString().drop(1).dropLast(1)
        println(result)
        return result
    }

    // this contains also the table with the states/templates
    fun getCompareDataView(compareDTO: CompareDTO): String {
        val uri = buildCompareEndpointURI()
        val requestBody = mapOf(
            CompareDTO::baselineTags.name to JSONObject(compareDTO.baselineTags).toString(),
            CompareDTO::candidateTags.name to JSONObject(compareDTO.candidateTags).toString(),
            CompareDTO::privateKey.name to compareDTO.privateKey
        )
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
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
        val logsReceipt = compareDTO.logsReceiptId?.let {
            logsReceiptStorageService.findLogsReceiptById(it)
        }
        return mapper.readValue<CompareDataResponse>(getCompareDataView(compareDTO))
    }

    fun getCompareByID(compareId: String?, user: User): List<HitsCompareDataPoint> {
        val chartRequest = ChartRequest(
            applicationId = null,
            chartConfig = ChartConfig(
                mapOf(
                    "type" to "util",
                    "feature" to "compare_id",
                    "indexType" to "verifications",
                    "compareId" to (compareId ?: "")
                )
            )
        )
        val getChartDataQuery = chartsService.getChartQuery(user.id, chartRequest)
        val verification =
            mapper.readValue<TableCompare>(chartsRepository.getData(getChartDataQuery, "${user.key}_verifications"))
        return verification.hits.hits
    }

    fun deleteCompareByID(compareId: String, user: User): String {
        return elasticsearchService.deleteByIndexAndDocID("${user.key}_verifications", compareId)
    }

    fun updateCompareStatusByID(compareId: String, compareStatus: Long, user: User): String {
        return elasticsearchService.updateStatusByIndexAndDocID(compareStatus, "${user.key}_verifications", compareId)
    }

    fun getAnalyticsIssuesKPI(user: User, baselineTags: Map<String, String>): List<VerticalBarBucket> {
        val chartRequest = ChartRequest(
            applicationId = null,
            chartConfig = ChartConfig(
                mapOf(
                    "type" to "util",
                    "feature" to "compare_analytics_issues",
                    "indexType" to "verifications",
                    "baselineTags" to getBaselineTagsQuery(baselineTags)
                )
            )
        )
        val getChartDataQuery = chartsService.getChartQuery(user.id, chartRequest)
        val chart = mapper.readValue<VerticalBarChartData>(chartsRepository.getData(getChartDataQuery, "${user.key}_verifications"))
        return chart.aggregations.listAggregations.buckets
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
