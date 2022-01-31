package ai.logsight.backend.charts

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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

@Service
class ESChartsServiceImpl(private val chartsRepository: ESChartRepository) : ChartsService {
    val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    override fun createHeatMap(getChartDataQuery: GetChartDataQuery): HeatmapChart {
        // get the String response from elasticsearch and map it into a HeatMapData Object.
        val heatMapData = mapper.readValue<HeatMapData>(chartsRepository.getData(getChartDataQuery))
        // map the HeatMapDataObject into HeatMapChart Object
        val heatMapSeries = mutableListOf<ChartSeries>()
        heatMapData.aggregations.listAggregations.buckets.forEach {
            val heatMapListPoints = mutableListOf<ChartSeriesPoint>()
            for (i in it.listBuckets.buckets) {
                val name = i.key.toString().split("_")
                    .subList(1, i.key.toString().split("_").size - 2).joinToString("  ")
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
        val barChartData = mapper.readValue<BarChartData>(chartsRepository.getData(getChartDataQuery))
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
        val pieChartData = mapper.readValue<PieChartData>(chartsRepository.getData(getChartDataQuery))
        val pieChartSeries = mutableListOf<ChartSeriesPoint>()
        pieChartData.aggregations.javaClass.kotlin.memberProperties.forEach {
            pieChartSeries.add(ChartSeriesPoint(name = it.name, it.get(pieChartData.aggregations) as Double))
        }
        return PieChart(data = pieChartSeries)
    }

    override fun createTableChart(getChartDataQuery: GetChartDataQuery): TableChart {
        val tableChartData = mapper.readValue<TableChartData>(chartsRepository.getData(getChartDataQuery))
        return TableChart(data = tableChartData.hits.hits)
    }
}
