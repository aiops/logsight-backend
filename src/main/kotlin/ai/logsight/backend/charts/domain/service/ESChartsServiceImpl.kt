package ai.logsight.backend.charts.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.charts.BarChart
import ai.logsight.backend.charts.domain.charts.HeatmapChart
import ai.logsight.backend.charts.domain.charts.PieChart
import ai.logsight.backend.charts.domain.charts.TableChart
import ai.logsight.backend.charts.domain.charts.models.ChartSeries
import ai.logsight.backend.charts.domain.charts.models.ChartSeriesPoint
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.BarChartData
import ai.logsight.backend.charts.repository.entities.elasticsearch.HeatMapData
import ai.logsight.backend.charts.repository.entities.elasticsearch.PieChartData
import ai.logsight.backend.charts.repository.entities.elasticsearch.TableChartData
import ai.logsight.backend.charts.repository.entities.elasticsearch.ValueResultBucket
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.ApplicationIndicesBuilder
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
    private val applicationStorageService: ApplicationStorageService,
    private val userStorageService: UserStorageService,
    private val applicationIndicesBuilder: ApplicationIndicesBuilder
) : ChartsService {
    val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    private val logger = LoggerImpl(ESChartsServiceImpl::class.java)

    override fun createHeatMap(getChartDataQuery: GetChartDataQuery): HeatmapChart {
        // get the String response from elasticsearch and map it into a HeatMapData Object.
        val applicationIndices = applicationIndicesBuilder.buildIndices(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        logger.debug("Obtained application indices: $applicationIndices.", this::createHeatMap.name)
        val heatMapData = mapper.readValue<HeatMapData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        logger.debug(
            "Obtained data from elasticsearch indices and successfully converted into an object",
            this::createHeatMap.name
        )
        // map the HeatMapDataObject into HeatMapChart Object
        val heatMapSeries = mutableListOf<ChartSeries>()
        logger.debug("Mapping the data to an output chart.", this::createHeatMap.name)
        heatMapData.aggregations.listAggregations.buckets.forEach {
            val heatMapListPoints = mutableListOf<ChartSeriesPoint>()
            for (i in it.listBuckets.buckets) {
                val name = i.key.split("_")
                    .subList(1, i.key.split("_").size - 1)
                    .joinToString("_")
                val app = applicationStorageService.findApplicationByUserAndName(user = getChartDataQuery.user, name)
                heatMapListPoints.add(
                    ChartSeriesPoint(
                        name = name,
                        value = i.valueData.value,
                        applicationId = app.id
                    )
                )
            }
            heatMapSeries.add(ChartSeries(name = it.date.toString(), series = heatMapListPoints))
        }
        return HeatmapChart(data = heatMapSeries)
    }

    override fun createBarChart(getChartDataQuery: GetChartDataQuery): BarChart {
        // get the String response from elasticsearch and map it into a BarChartData Object.
        val applicationIndices = applicationIndicesBuilder.buildIndices(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        logger.debug("Obtained application indices: $applicationIndices.", this::createBarChart.name)
        val barChartData =
            mapper.readValue<BarChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        logger.debug(
            "Obtained data from elasticsearch indices and successfully converted into an object",
            this::createBarChart.name
        )
        // map the BarChartData into BarChart Object
        logger.debug("Mapping the data to an output chart.", this::createBarChart.name)
        val barChartSeries = mutableListOf<ChartSeries>()
        val barChartSeriesPoints = mutableListOf<ChartSeriesPoint>()
        barChartData.aggregations.listAggregations.buckets.forEach {
            barChartSeriesPoints.add(ChartSeriesPoint(name = "Anomalies", value = it.bucketPrediction.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "ERROR", value = it.bucketError.value))
            barChartSeriesPoints.add(ChartSeriesPoint(name = "WARN", value = it.bucketWarning.value))
            barChartSeries.add(ChartSeries(name = it.date.toString(), series = barChartSeriesPoints))
        }
        return BarChart(data = barChartSeries)
    }

    override fun createPieChart(getChartDataQuery: GetChartDataQuery): PieChart {
        // get the String response from elasticsearch and map it into a BarChartData Object.
        val applicationIndices = applicationIndicesBuilder.buildIndices(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        logger.debug("Obtained application indices: $applicationIndices.", this::createPieChart.name)
        val pieChartData =
            mapper.readValue<PieChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        logger.debug(
            "Obtained data from elasticsearch indices and successfully converted into an object",
            this::createPieChart.name
        )
        logger.debug("Mapping the data to an output chart.", this::createPieChart.name)
        val pieChartSeries = mutableListOf<ChartSeriesPoint>()
        pieChartData.aggregations.javaClass.kotlin.memberProperties.forEach {
            pieChartSeries.add(
                ChartSeriesPoint(
                    name = it.name.uppercase(),
                    (it.get(pieChartData.aggregations) as ValueResultBucket).value
                )
            )
        }
        return PieChart(data = pieChartSeries)
    }

    override fun createTableChart(getChartDataQuery: GetChartDataQuery): TableChart {
        val applicationIndices = applicationIndicesBuilder.buildIndices(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        logger.debug("Obtained application indices: $applicationIndices.", this::createTableChart.name)
        val tableChartData =
            mapper.readValue<TableChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        logger.debug(
            "Obtained data from elasticsearch indices and successfully converted into an object",
            this::createTableChart.name
        )
        logger.debug("Mapping the data to an output chart.", this::createTableChart.name)
        tableChartData.hits.hits.sortedByDescending { it.source.totalScore }

        return TableChart(
            data = tableChartData.hits.hits.take(getChartDataQuery.chartConfig.numElements?:tableChartData.hits.hits.size).map {
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

    override fun getChartQuery(userId: UUID, createChartRequest: ChartRequest): GetChartDataQuery {
        val user = userStorageService.findUserById(userId)
        val application: Application? = try {
            createChartRequest.applicationId?.let { applicationStorageService.findApplicationById(it) }
        } catch (e: ApplicationNotFoundException) {
            null
        }
        return GetChartDataQuery(
            credentials = Credentials(user.email, user.key),
            chartConfig = createChartRequest.chartConfig,
            user = user,
            application = application
        )
    }
}
