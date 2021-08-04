package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.*
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.repositories.elasticsearch.ChartsRepository
import com.loxbear.logsight.services.ApplicationService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import utils.UtilsService
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class ChartsService(val repository: ChartsRepository,
                    val applicationService: ApplicationService) {

    fun getAnomaliesBarChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): List<LineChart> {

        return repository.getAnomaliesBarChartData(es_index_user_app, startTime, stopTime, userKey)
            .aggregations.listAggregations.buckets.map {
                val name = it.date.toDateTime()
                val series = it.listBuckets.buckets.map { it2 ->
                    var tmp = ""
                    if (it2.key.toString() == "0") {
                        tmp = "Normal"
                    } else {
                        tmp = "Anomaly"
                    }
                    LineChartSeries(name = tmp, value = it2.docCount)
                }
                LineChart(name, series)
            }
    }

    fun getLogLevelPieChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LogLevelPieChart {
        val data = mutableListOf<LogLevelPoint>()

        repository.getLogLevelPieChartData(es_index_user_app, startTime, stopTime, userKey).aggregations.listAggregations.buckets.forEach {
            data.add(LogLevelPoint(name = it.key, value = it.docCount, extra = PieExtra(code = "logs")))
        }
        return LogLevelPieChart(data = data)
    }

    fun getLogLevelStackedLineChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LogLevelStackedLineChart {
        val dict = mutableMapOf<String, MutableList<StackedLogLevelPoint>>()
        val res = repository.getLogLevelStackedLineChartData(es_index_user_app, startTime, stopTime, userKey).aggregations.listAggregations.buckets
        res.forEach {
            for (i in it.listBuckets.buckets) {
                val list = dict[i.key] ?: mutableListOf()
                list.add(StackedLogLevelPoint(it.date.toDateTime(), i.docCount))
                dict[i.key] = list
            }
        }
        val stackedSeries = mutableListOf<StackedLogLevelSeries>()
        for (i in dict.keys) {
            stackedSeries.add(StackedLogLevelSeries(name = i, series = dict.getValue(i)))
        }
        return LogLevelStackedLineChart(data = stackedSeries)
    }

    fun getSystemOverviewHeatmapChart(esIndexUserAppLogAd: String,
                                      startTime: String, stopTime: String, user: LogsightUser): SystemOverviewHeatmapChart {
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        val heatMapLogLevelSeries = mutableListOf<HeatMapLogLevelSeries>()
        repository.getSystemOverviewHeatmapChartData(esIndexUserAppLogAd,
            startTime,
            stopTime, user.key).aggregations.listAggregations.buckets.forEach {
            val listPoints = mutableListOf<HeatMapLogLevelPoint>()
            for (i in it.listBuckets.buckets) {
                listPoints.add(HeatMapLogLevelPoint(
                    name = i.key.split("_").subList(1, i.key.split("_").size - 1).joinToString("  "),
                    value = i.valueData.value,
                    extra = PieExtra(""),
                    id = UtilsService.getApplicationIdFromIndex(applications, i.key),
                    count = i.docCount)
                )
            }

            heatMapLogLevelSeries.add(HeatMapLogLevelSeries(name = it.date.toDateTime(), series = listPoints))
        }
        return SystemOverviewHeatmapChart(data = heatMapLogLevelSeries)
    }

    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm"))
    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun ZonedDateTime.toTimeWithSeconds(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

}