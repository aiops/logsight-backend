package ai.logsight.backend.charts

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.charts.BarChart
import ai.logsight.backend.charts.domain.charts.HeatmapChart
import ai.logsight.backend.charts.domain.charts.PieChart
import ai.logsight.backend.charts.domain.charts.TableChart
import ai.logsight.backend.charts.domain.charts.models.ChartSeries
import ai.logsight.backend.charts.domain.charts.models.ChartSeriesPoint
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.*
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.loxbear.logsight.charts.data.IncidentRow
import org.json.JSONObject
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

@Service
class ESChartsServiceImpl(
    private val chartsRepository: ESChartRepository,
    private val applicationStorageService: ApplicationStorageService
) : ChartsService {
    val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    override fun createHeatMap(getChartDataQuery: GetChartDataQuery): HeatmapChart {
        // get the String response from elasticsearch and map it into a HeatMapData Object.
        val applicationIndices = getApplicationIndexes(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        println(chartsRepository.getData(getChartDataQuery, applicationIndices))
        val heatMapData = mapper.readValue<HeatMapData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        // map the HeatMapDataObject into HeatMapChart Object
        val heatMapSeries = mutableListOf<ChartSeries>()
        heatMapData.aggregations.listAggregations.buckets.forEach {
            val heatMapListPoints = mutableListOf<ChartSeriesPoint>()
            for (i in it.listBuckets.buckets) {
                val name = i.key.split("_").subList(1, i.key.split("_").size - 1).joinToString(" ")
                heatMapListPoints.add(
                    ChartSeriesPoint(
                        name = name,
                        value = i.valueData.value,
                    )
                )
            }
            heatMapSeries.add(ChartSeries(name = it.date.toString(), series = heatMapListPoints))
        }
        return HeatmapChart(data = heatMapSeries)
    }

    override fun createBarChart(getChartDataQuery: GetChartDataQuery): BarChart {
        // get the String response from elasticsearch and map it into a BarChartData Object.
        val applicationIndices = getApplicationIndexes(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )

        val barChartData =
            mapper.readValue<BarChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        // map the BarChartData into BarChart Object
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
        val applicationIndices = getApplicationIndexes(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )

        val pieChartData =
            mapper.readValue<PieChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        val pieChartSeries = mutableListOf<ChartSeriesPoint>()
        pieChartData.aggregations.javaClass.kotlin.memberProperties.forEach {
            pieChartSeries.add(ChartSeriesPoint(name = it.name.uppercase(), (it.get(pieChartData.aggregations) as ValueResultBucket).value))
        }
        return PieChart(data = pieChartSeries)
    }

    override fun createTableChart(getChartDataQuery: GetChartDataQuery): TableChart {
        val applicationIndices = getApplicationIndexes(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        val tableChartData =
            mapper.readValue<TableChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        return TableChart(
            data = tableChartData.hits.hits.map {
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

    fun getApplicationIndexes(user: User, application: Application?, indexType: String) =
        applicationStorageService.findAllApplicationsByUser(user).filter {
            application?.let { application -> application.id == it.id } ?: true
        }.joinToString(",") { "${user.key.lowercase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_$indexType" }
}
