package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.*
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.repositories.elasticsearch.ChartsRepository
import com.loxbear.logsight.services.ApplicationService
import org.json.JSONObject
import org.springframework.stereotype.Service
import utils.UtilsService
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class ChartsService(val repository: ChartsRepository,
                    val applicationService: ApplicationService) {

    fun getAnomaliesBarChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String
    ): List<LineChart> {

        val data =  repository.getAnomaliesBarChartData(es_index_user_app, startTime, stopTime, userKey)
            .aggregations.listAggregations.buckets.map {
                val name = it.date.toDateTime()
                val series = it.listBuckets.buckets.map { it2 ->
                    val tmp = if (it2.key.toString() == "0") {
                        "Normal"
                    } else {
                        "Anomaly"
                    }
                    LineChartSeries(name = tmp, value = it2.docCount)
                }
                LineChart(name, series)
            }
        return data
    }


    fun getAnomaliesBarChartDataAgg(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        userKey: String
    ): List<LineChart>? {
        var seriesList = mutableListOf<LineChartSeries>()
        val resultList = mutableListOf<LineChart>()
        try {
            val data = repository.getAnomaliesBarChartDataAgg(es_index_user_app, startTime, stopTime, userKey)
            JSONObject(data).getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").forEach {
                seriesList.add(LineChartSeries(name = "Faults", value = JSONObject(it.toString()).getJSONObject("listBucketsPrediction").getDouble("value")))
                seriesList.add(LineChartSeries(name = "ERROR", value = JSONObject(it.toString()).getJSONObject("listBucketsError").getDouble("value")))
                seriesList.add(LineChartSeries(name = "WARN", value = JSONObject(it.toString()).getJSONObject("listBucketsWarning").getDouble("value")))

                resultList.add(LineChart(name = ZonedDateTime.parse(JSONObject(it.toString()).getString("key_as_string")).toDateTime(), seriesList))
                seriesList = mutableListOf<LineChartSeries>()
            }
        }catch (e: Exception){
            return null
        }

        return resultList

    }


    fun getLogLevelPieChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LogLevelPieChart? {
        val data = mutableListOf<LogLevelPoint>()
        try{
            val esData = JSONObject(repository.getLogLevelPieChartDataAgg(es_index_user_app, startTime, stopTime, userKey))
                .getJSONObject("aggregations")
            esData.keys().forEach{
                data.add(LogLevelPoint(name = it.toString(), value = esData.getJSONObject(it.toString()).getDouble("value"), extra = PieExtra(code = "logs")))
            }
        }catch (e: Exception){
            return null
        }
        //        println(esData)
//            .aggregations.listAggregations.buckets.forEach {
//            data.add(LogLevelPoint(name = it.key, value = it.docCount, extra = PieExtra(code = "logs")))
        return LogLevelPieChart(data = data)
    }

//        repository.getLogLevelPieChartData(es_index_user_app, startTime, stopTime, userKey).aggregations.listAggregations.buckets.forEach {
//            data.add(LogLevelPoint(name = it.key, value = it.docCount, extra = PieExtra(code = "logs")))
//        }

//    }

    fun getLogLevelStackedLineChartData(es_index_user_app: String, startTime: String, stopTime: String, userKey: String): LogLevelStackedLineChart? {
        val dict = mutableMapOf<String, MutableList<StackedLogLevelPoint>>()
        try {
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
        }catch (e: Exception){
            return null
        }

    }

    fun getSystemOverviewHeatmapChart(
        esIndexUserAppLogAd: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        compareTagId: String?,
        baselineTagId: String?,
        intervalAggregate: String?
    ): SystemOverviewHeatmapChart? {
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        val heatMapLogLevelSeries = mutableListOf<HeatMapLogLevelSeries>()

        try {
            repository.getSystemOverviewHeatmapChartData(esIndexUserAppLogAd,
                startTime,
                stopTime, user, compareTagId, baselineTagId, intervalAggregate).aggregations.listAggregations.buckets.forEach {
                val listPoints = mutableListOf<HeatMapLogLevelPoint>()
                for (i in it.listBuckets.buckets) {
                    var name = ""
                    if (compareTagId == null && baselineTagId == null){
                        name = i.key.split("_").subList(1, i.key.split("_").size - 1).joinToString("  ")
                    }else{
                        name = i.key.split("_").subList(1, i.key.split("_").size - 2).joinToString("  ")
                    }
                    listPoints.add(HeatMapLogLevelPoint(
                        name = name,
                        value = i.valueData.value,
                        extra = PieExtra(""),
                        id = UtilsService.getApplicationIdFromIndex(applications, i.key),
                        count = i.docCount)
                    )
                }

                heatMapLogLevelSeries.add(HeatMapLogLevelSeries(name = it.date.toDateTime(), series = listPoints))
            }
        }catch (e: Exception){
            return null
        }
        return SystemOverviewHeatmapChart(data = heatMapLogLevelSeries)
    }

    fun getNewTemplatesBarChartData(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        baselineTagId: String?,
        compareTagId: String?,
        intervalAggregate: String
    ): MutableList<LineChart> {
        val dataList = mutableListOf<LineChart>()
        JSONObject(repository.getNewTemplatesBarChartData(es_index_user_app, startTime, stopTime, user, baselineTagId, compareTagId, intervalAggregate))
            .getJSONObject("aggregations")
            .getJSONObject("listAggregations")
            .getJSONArray("buckets").forEach {
                val name = JSONObject(it.toString()).getString("key_as_string")
                var newNormal = 0.0
                var newAnomalies = 0.0
                if (!JSONObject(it.toString()).getJSONObject("new_normal").toString().contains("null")) {
                    newNormal = JSONObject(it.toString()).getJSONObject("new_normal").getDouble("value")
                    newAnomalies = JSONObject(it.toString()).getJSONObject("new_anomalies").getDouble("value")
                }else{
                    newNormal = 0.0
                    newAnomalies = 0.0
                }

                val lineChartList = mutableListOf<LineChartSeries>()
                lineChartList.add(LineChartSeries(name = "Anomaly", value = newAnomalies))
                lineChartList.add(LineChartSeries(name = "Normal", value = newNormal))
                dataList.add(LineChart(name = name, series = lineChartList))
            }
        return dataList
    }

    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm"))
    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun String.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

    fun ZonedDateTime.toTimeWithSeconds(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

}
