package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LogQualityTable
import com.loxbear.logsight.repositories.elasticsearch.LogCompareRepository
import com.loxbear.logsight.services.ApplicationService
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
class LogCompareService(
    val logCompareRepository: LogCompareRepository,
    val applicationService: ApplicationService,
) {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val restTemplate = RestTemplateBuilder()
        .build()

    @Value("\${app.baseUrl}")
    private val baseUrl: String? = null

    fun getApplicationVersions(
        applicationsIndexes: String,
        user: LogsightUser
    ): MutableList<String> {
        val dataList = mutableListOf<String>()
        JSONObject(logCompareRepository.getApplicationVersions(applicationsIndexes, user.key))
            .getJSONObject("aggregations")
            .getJSONObject("listAggregations")
            .getJSONArray("buckets").forEach {
                dataList.add(JSONObject(it.toString()).getString("key"))
            }
        return dataList
    }

    fun getLogCountBar(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        intervalAggregate: String,
        tag: String
    ): List<LineChart> {
        val resp = JSONObject(
            logCompareRepository.getLogCountBar(
                applicationsIndexes,
                startTime,
                stopTime,
                user.key,
                intervalAggregate,
                tag
            )
        )
        val lineChartSeries =
            resp.getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").map {
                val obj = JSONObject(it.toString())
                val odtInstanceAtOffset = OffsetDateTime.parse(obj.getString("key_as_string"))
                val odtInstanceAtUTC = odtInstanceAtOffset.withOffsetSameInstant(ZoneOffset.UTC)
                val date = odtInstanceAtUTC.toZonedDateTime()
                LineChartSeries(date.toDateTime(), obj.getDouble("doc_count"))
            }
        return listOf(LineChart("Log Count", lineChartSeries))
    }

    fun getAnomaliesBarChartData(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        intervalAggregate: String,
        tag: String
    ): List<LineChart> {

        return logCompareRepository.getAnomaliesBarChartData(
            applicationsIndexes,
            startTime,
            stopTime,
            user.key,
            intervalAggregate,
            tag
        )
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

    fun getCompareTemplatesHorizontalBar(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        intervalAggregate: String,
        baselineTagId: String,
        compareTagId: String
    ): List<LineChart> {
        val data = JSONObject(
            logCompareRepository.getCompareTemplatesHorizontalBar(
                applicationsIndexes,
                startTime,
                stopTime,
                user.key,
                intervalAggregate,
                baselineTagId,
                compareTagId
            )
        )
            .getJSONObject("aggregations")
            .getJSONObject("listAggregations")
            .getJSONArray("buckets").map {
                val jsonData = JSONObject(it.toString())
                val name = jsonData.getString("key")
                val series = jsonData.getJSONObject("tags").getJSONArray("buckets").map { it2 ->
                    val jsonData2 = JSONObject(it2.toString())
                    LineChartSeries(name = jsonData2.getString("key"), value = jsonData2.getDouble("doc_count"))
                }
                LineChart(name, series)
            }
        return data
    }

    fun getLogCompareData(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        baselineTagId: String,
        compareTagId: String
    ) {
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        val dataList = mutableListOf<LogQualityTable>()
        val jsonData = JSONObject(
            logCompareRepository.getLogCompareData(
                applicationsIndexes,
                startTime,
                stopTime,
                user.key,
                baselineTagId,
                compareTagId
            )
        )
        println(jsonData)
    }

    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun LocalDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"))
}

