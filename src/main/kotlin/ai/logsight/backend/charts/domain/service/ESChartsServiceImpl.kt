package ai.logsight.backend.charts.domain.service

import ai.logsight.backend.charts.domain.charts.BarChart
import ai.logsight.backend.charts.domain.charts.PieChart
import ai.logsight.backend.charts.domain.charts.TableChart
import ai.logsight.backend.charts.domain.charts.models.ChartSeries
import ai.logsight.backend.charts.domain.charts.models.ChartSeriesPoint
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.*
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.QueryBuilderHelper
import ai.logsight.backend.compare.controller.request.TagEntry
import ai.logsight.backend.compare.dto.Tag
import ai.logsight.backend.compare.dto.TagKey
import ai.logsight.backend.incidents.controller.request.GetAllIncidentsRequest
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.loxbear.logsight.charts.data.IncidentRow
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.full.memberProperties

@Service
class ESChartsServiceImpl(
    private val chartsRepository: ESChartRepository,
    private val userStorageService: UserStorageService,
    private val queryBuilderHelper: QueryBuilderHelper
) : ChartsService {
    val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    private val logger = LoggerImpl(ESChartsServiceImpl::class.java)

    override fun createBarChart(getChartDataQuery: GetChartDataQuery): BarChart {
        var barChartSeries = mutableListOf<ChartSeries>()
        if (getChartDataQuery.chartConfig.parameters["feature"] == "system_overview") {
            barChartSeries = createDashboardBarChart(getChartDataQuery)
        } else if (getChartDataQuery.chartConfig.parameters["feature"] == "compare_analytics_risk") {
            barChartSeries = createCompareAnalyticsBarChart(getChartDataQuery)
        } else if (getChartDataQuery.chartConfig.parameters["feature"] == "compare_analytics_verification_frequency") {
            barChartSeries = createCompareAnalyticsBarChartFrequency(getChartDataQuery)
        } else if (getChartDataQuery.chartConfig.parameters["feature"] == "compare_analytics_verification_velocity") {
            barChartSeries = createCompareAnalyticsBarChartVelocity(getChartDataQuery)
        } else if (getChartDataQuery.chartConfig.parameters["feature"] == "compare_analytics_verification_velocity_min_max") {
            barChartSeries = createCompareAnalyticsBarChartVelocityMinMax(getChartDataQuery)
        } else if (getChartDataQuery.chartConfig.parameters["feature"] == "compare_analytics_verification_failure_ratio") {
            barChartSeries = createCompareAnalyticsBarChartVelocityFailureRatio(getChartDataQuery)
        }
        return BarChart(data = barChartSeries)
    }

    fun createCompareAnalyticsBarChartVelocityFailureRatio(getChartDataQuery: GetChartDataQuery): MutableList<ChartSeries> {
        getChartDataQuery.chartConfig.parameters["baselineTags"] =
            queryBuilderHelper.getBaselineTagsQuery(getChartDataQuery.chartConfig.parameters["baselineTags"] as Map<String, String>)
        val barChartData = mapper.readValue<BarChartData>(
            chartsRepository.getData(
                getChartDataQuery,
                "*_${getChartDataQuery.chartConfig.parameters["indexType"]}"
            )
        )
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Failure ratio", value = it.bucketFailureRatio.value))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return barChartSeries
    }

    fun createCompareAnalyticsBarChart(getChartDataQuery: GetChartDataQuery): MutableList<ChartSeries> {
        getChartDataQuery.chartConfig.parameters["baselineTags"] =
            queryBuilderHelper.getBaselineTagsQuery(getChartDataQuery.chartConfig.parameters["baselineTags"] as Map<String, String>)
        val barChartData = mapper.readValue<BarChartData>(
            chartsRepository.getData(
                getChartDataQuery,
                "*_${getChartDataQuery.chartConfig.parameters["indexType"]}"
            )
        )
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Max risk", value = it.bucketMaxRisk.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Min risk", value = it.bucketMinRisk.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Mean risk", value = it.bucketMeanRisk.value))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return barChartSeries
    }

    fun createCompareAnalyticsBarChartVelocity(getChartDataQuery: GetChartDataQuery): MutableList<ChartSeries> {
        getChartDataQuery.chartConfig.parameters["baselineTags"] =
            queryBuilderHelper.getBaselineTagsQuery(getChartDataQuery.chartConfig.parameters["baselineTags"] as Map<String, String>)
        val barChartData = mapper.readValue<BarChartData>(
            chartsRepository.getData(
                getChartDataQuery,
                "*_${getChartDataQuery.chartConfig.parameters["indexType"]}"
            )
        )
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Velocity", value = it.bucketVelocity.value))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return barChartSeries
    }

    fun createCompareAnalyticsBarChartVelocityMinMax(getChartDataQuery: GetChartDataQuery): MutableList<ChartSeries> {
        getChartDataQuery.chartConfig.parameters["baselineTags"] =
            queryBuilderHelper.getBaselineTagsQuery(getChartDataQuery.chartConfig.parameters["baselineTags"] as Map<String, String>)
        val barChartData = mapper.readValue<BarChartData>(
            chartsRepository.getData(
                getChartDataQuery,
                "*_${getChartDataQuery.chartConfig.parameters["indexType"]}"
            )
        )
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Min velocity", value = it.bucketMinVelocity.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Max velocity", value = it.bucketMaxVelocity.value))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return barChartSeries
    }

    fun createCompareAnalyticsBarChartFrequency(getChartDataQuery: GetChartDataQuery): MutableList<ChartSeries> {
        getChartDataQuery.chartConfig.parameters["baselineTags"] =
            queryBuilderHelper.getBaselineTagsQuery(getChartDataQuery.chartConfig.parameters["baselineTags"] as Map<String, String>)
        val barChartData = mapper.readValue<BarChartData>(
            chartsRepository.getData(
                getChartDataQuery,
                "*_${getChartDataQuery.chartConfig.parameters["indexType"]}"
            )
        )
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Count", value = it.docCount))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return barChartSeries
    }

    fun createDashboardBarChart(getChartDataQuery: GetChartDataQuery): MutableList<ChartSeries> {
        val barChartData =
            mapper.readValue<BarChartData>(
                chartsRepository.getData(
                    getChartDataQuery,
                    getChartDataQuery.chartConfig.parameters["indexType"].toString()
                )
            )
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Anomalies", value = it.bucketPrediction.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "ERROR", value = it.bucketError.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "WARN", value = it.bucketWarning.value))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return barChartSeries
    }

    override fun createPieChart(getChartDataQuery: GetChartDataQuery): PieChart {
        // get the String response from elasticsearch and map it into a BarChartData Object.
        val pieChartData =
            mapper.readValue<PieChartData>(
                chartsRepository.getData(
                    getChartDataQuery,
                    getChartDataQuery.chartConfig.parameters["indexType"].toString()
                )
            )
        logger.debug(
            "Obtained data from elasticsearch indices and successfully converted into an object",
            this::createPieChart.name
        )
        logger.debug("Mapping the data to an output chart.", this::createPieChart.name)
        val pieChartSeries = mutableListOf<ChartSeriesPoint>()
        pieChartData.aggregations.javaClass.kotlin.memberProperties.forEach {
            pieChartSeries.add(
                ChartSeriesPoint(
                    name = it.name.uppercase(), (it.get(pieChartData.aggregations) as ValueResultBucket).value
                )
            )
        }
        return PieChart(data = pieChartSeries)
    }

    override fun createTableChart(getChartDataQuery: GetChartDataQuery): TableChart {
        val tableChartData =
            mapper.readValue<TableChartData>(
                chartsRepository.getData(
                    getChartDataQuery,
                    getChartDataQuery.chartConfig.parameters["indexType"].toString()
                )
            )
        logger.debug(
            "Obtained data from elasticsearch indices and successfully converted into an object",
            this::createTableChart.name
        )
        logger.debug("Mapping the data to an output chart.", this::createTableChart.name)
        tableChartData.hits.hits.sortedByDescending { it.source.totalScore }
        val numElements =
            if (getChartDataQuery.chartConfig.parameters.containsKey("numElements")) getChartDataQuery.chartConfig.parameters["numElements"] as Int else null
        return TableChart(
            data = tableChartData.hits.hits.take(numElements ?: tableChartData.hits.hits.size).map {
                IncidentRow(
                    applicationId = it.source.applicationId,
                    indexName = it.indexName,
                    timestamp = it.source.timestamp,
                    startTimestamp = it.source.startTimestamp,
                    stopTimestamp = it.source.stopTimestamp,
                    newTemplates = it.source.newTemplates.toString(), // jsonData.getJSONObject("_source")["first_log"].toString()
                    semanticAD = it.source.semanticAD.toString(), // jsonData.getJSONObject("_source")["first_log"].toString()
                    countAD = it.source.countAD.toString(),
                    scAnomalies = it.source.scAnomalies.toString(),
                    logs = it.source.logData.toString(),
                    totalScore = it.source.totalScore
                )
            }
        )
    }

    fun getCompareByID(compareId: String?, user: User): List<HitsCompareDataPoint> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "compare_id",
                    "indexType" to "verifications",
                    "propertyId" to (compareId ?: "")
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val verification =
            mapper.readValue<TableCompare>(
                chartsRepository.getData(
                    getChartDataQuery,
                    "${user.key}_${chartRequest.chartConfig.parameters["indexType"]}"
                )
            )
        return verification.hits.hits
    }

    fun getIncidentByID(incidentId: String?, user: User): List<HitsIncidentDataPoint> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "incidents_id",
                    "indexType" to "incidents",
                    "propertyId" to (incidentId ?: "")
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val verification =
            mapper.readValue<TableIncident>(
                chartsRepository.getData(
                    getChartDataQuery,
                    "${user.key}_${chartRequest.chartConfig.parameters["indexType"]}"
                )
            )
        return verification.hits.hits
    }

    fun getAllCompares(user: User): List<HitsCompareAllDataPoint> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "compare_id",
                    "indexType" to "verifications",
                    "propertyId" to ""
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val verification =
            mapper.readValue<TableCompareAll>(
                chartsRepository.getData(
                    getChartDataQuery,
                    "${user.key}_${chartRequest.chartConfig.parameters["indexType"]}"
                )
            )
        return verification.hits.hits
    }

    fun getAllIncidents(user: User, getAllIncidentsRequest: GetAllIncidentsRequest): List<HitsIncidentAllDataPoint> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "incidents_all",
                    "indexType" to "incidents",
                    "propertyId" to "",
                    "startTime" to getAllIncidentsRequest.startTime,
                    "stopTime" to getAllIncidentsRequest.stopTime
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val incidents =
            mapper.readValue<TableIncidentAll>(
                chartsRepository.getData(
                    getChartDataQuery,
                    "${user.key}_${chartRequest.chartConfig.parameters["indexType"]}"
                )
            )
        return incidents.hits.hits
    }


    fun getCompareTagFilter(user: User, listTags: List<TagEntry>, applicationIndices: String): List<TagKey> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "filter_tags",
                    "indexType" to "pipeline",
                    "filter" to queryBuilderHelper.getTagsFilterQuery(listTags, applicationIndices)
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val tagData = mapper.readValue<TagData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        val tagDataFiltered = tagData.aggregations.listAggregations.buckets.filter { itFilter ->
            !listTags.map { itMap1 -> itMap1.tagName }.contains(itFilter.tagValue)
        }.map { itMap2 -> TagKey(tagName = itMap2.tagValue, itMap2.tagCount) }
        return tagDataFiltered
    }

    fun getCompareTagValues(
        user: User,
        tagName: String,
        applicationIndices: String,
        listTags: List<TagEntry>
    ): List<Tag> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "versions",
                    "indexType" to "pipeline",
                    "filter" to queryBuilderHelper.getTagsFilterQuery(listTags, applicationIndices),
                    "field" to tagName
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val tagValues = mapper.readValue<TagData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        return tagValues.aggregations.listAggregations.buckets.map {
            Tag(tagName = tagName, tagValue = it.tagValue, tagCount = it.tagCount)
        }
    }

    override fun getAnalyticsIssuesKPI(userId: UUID, baselineTags: Map<String, String>): Map<Long, Long> {
        val user = userStorageService.findUserById(userId)
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "compare_analytics_issues",
                    "indexType" to "verifications",
                    "baselineTags" to queryBuilderHelper.getBaselineTagsQuery(baselineTags)
                )
            )
        )
        val getChartDataQuery = getChartQuery(user.id, chartRequest)
        val chart = mapper.readValue<VerticalBarChartData>(
            chartsRepository.getData(
                getChartDataQuery, "${user.key}_verifications"
            )
        )
        return chart.aggregations.listAggregations.buckets.map { it.status to it.count }.toMap()
    }

    override fun getChartQuery(userId: UUID, createChartRequest: ChartRequest): GetChartDataQuery {
        val user = userStorageService.findUserById(userId)
        return GetChartDataQuery(
            credentials = Credentials(user.email, user.key),
            chartConfig = createChartRequest.chartConfig,
            user = user,
        )
    }
}
