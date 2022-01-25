package ai.logsight.backend.charts

import ai.logsight.backend.charts.domain.charts.query.GetChartDataQuery
import ai.logsight.backend.charts.repository.ESChartRepository
import com.loxbear.logsight.charts.data.HeatMapLogLevelPoint
import com.loxbear.logsight.charts.data.HeatMapLogLevelSeries
import com.loxbear.logsight.charts.data.HeatmapChart
import com.loxbear.logsight.charts.data.PieExtra
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class ESChartsServiceImpl(
    private val chartsRepository: ESChartRepository
) : ChartsService {
    override fun createHeatmap(getChartDataQuery: GetChartDataQuery): HeatmapChart {
        val data = JSONObject(chartsRepository.getData(getChartDataQuery))
        val heatMapLogLevelSeries = mutableListOf<HeatMapLogLevelSeries>()

        data.getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").forEach {
            val jsonData = JSONObject(it.toString())
            val listPoints = mutableListOf<HeatMapLogLevelPoint>()

            for (i in jsonData.getJSONObject("listBuckets").getJSONArray("buckets")) {
                val bucket = JSONObject(i) // ova ne treba vaka
                val name = bucket.getJSONObject("key").toString().split("_")
                    .subList(1, bucket.getJSONObject("key").toString().split("_").size - 2).joinToString("  ")

                listPoints.add(
                    HeatMapLogLevelPoint(
                        name = name,
                        value = bucket.getJSONObject("valueData").getDouble("value"),
                        extra = PieExtra(""),
                        id = getChartDataQuery.applicationId.toString(),
                        count = bucket.getDouble("docCount")
                    )
                )
            }

            heatMapLogLevelSeries.add(HeatMapLogLevelSeries(name = jsonData.getString("date"), series = listPoints))
        }
        return HeatmapChart(data = heatMapLogLevelSeries)
    }
}
