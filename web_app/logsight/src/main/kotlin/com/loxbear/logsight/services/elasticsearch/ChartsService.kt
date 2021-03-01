package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.*
import com.loxbear.logsight.repositories.elasticsearch.ChartsRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

//[
//{
//    "name": "Germany",
//    "value": 40632,
//    "extra": {
//    "code": "de"
//}
//},
//{
//    "name": "United States",
//    "value": 50000,
//    "extra": {
//    "code": "us"
//}
//}]
@Service
class ChartsService(val repository: ChartsRepository) {

    fun getLineChartData(): LineChart {
        val labels = mutableListOf<String>()
        val normal = mutableListOf<Double>()
        val anomaly = mutableListOf<Double>()
        val dataSets = mutableListOf<DataSet>()
        repository.getLineChartData().aggregations.listAggregations.buckets.forEach {
            labels.add(it.date.toBookingTime())
            val dataKey0 = it.listBuckets.buckets[0].key
            if (dataKey0 == "normal") {
                normal.add(it.listBuckets.buckets[0].docCount)
            } else if (dataKey0 == "anomaly") {
                anomaly.add(it.listBuckets.buckets[0].docCount)
            }
            if (it.listBuckets.buckets.size > 1) {
                val dataKey1 = it.listBuckets.buckets[1].key
                if (dataKey1 == "normal") {
                    normal.add(it.listBuckets.buckets[1].docCount)
                } else if (dataKey1 == "anomaly") {
                    anomaly.add(it.listBuckets.buckets[1].docCount)
                }
            }
        }
        dataSets.add(DataSet(data = anomaly, label = "Anomaly"))
        dataSets.add(DataSet(data = normal, label = "Normal"))
        return LineChart(labels = labels, datasets = dataSets)
    }

    fun getLogLevelPieChartData(es_index_user_app: String, startTime: String, stopTime: String): LogLevelPieChart {
        val data = mutableListOf<LogLevelPoint>()

        repository.getLogLevelPieChartData(es_index_user_app, startTime, stopTime).aggregations.listAggregations.buckets.forEach {
            data.add(LogLevelPoint(name = it.key, value = it.docCount, extra = PieExtra(code = "logs")))
        }
        return LogLevelPieChart(data = data)
    }

    fun getLogLevelStackedLineChartData(es_index_user_app: String, startTime: String, stopTime: String): LogLevelStackedLineChart {
        val dict = mutableMapOf<String, MutableList<StackedLogLevelPoint>>()
        val values = mutableListOf<StackedLogLevelPoint>()
        repository.getLogLevelStackedLineChartData(es_index_user_app, startTime, stopTime).aggregations.listAggregations.buckets.forEach {

            for (i in it.listBuckets.buckets) {
                values.add(StackedLogLevelPoint(it.date.toBookingTime(), i.docCount))
                dict[i.key] = values
            }
        }
        val stackedSeries = mutableListOf<StackedLogLevelSeries>()
        for (i in dict.keys) {
            stackedSeries.add(StackedLogLevelSeries(name = i, series = dict.getValue(i)))
        }
        return LogLevelStackedLineChart(data = stackedSeries)
    }

    fun getSystemOverviewHeatmapChart(esIndexUserAppLogAd: String,
                                      startTime: String, stopTime: String): SystemOverviewHeatmapChart {
        val heatMapLogLevelSeries = mutableListOf<HeatMapLogLevelSeries>()
        repository.getSystemOverviewHeatmapChartData(esIndexUserAppLogAd,
            startTime,
            stopTime).aggregations.listAggregations.buckets.forEach {
            val listPoints = mutableListOf<HeatMapLogLevelPoint>()
            for (i in it.listBuckets.buckets) {
                listPoints.add(HeatMapLogLevelPoint(name = i.key.split("_")[1] + i.key.split("_")[2] + i.key.split("_")[3] , value = i.valueData.value, extra = PieExtra("")))
            }
            heatMapLogLevelSeries.add(HeatMapLogLevelSeries(name = it.date.toBookingTime(), series = listPoints))
        }

        return SystemOverviewHeatmapChart(data = heatMapLogLevelSeries)
    }


    fun ZonedDateTime.toBookingTime(): String = this.format(DateTimeFormatter.ofPattern("HH:mm"))

}
