package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHitCompare
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHitCompareCount
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LogCompareTable
import com.loxbear.logsight.repositories.elasticsearch.LogCompareRepository
import com.loxbear.logsight.services.ApplicationService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import utils.UtilsService
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Service
class LogCompareService(
    val logCompareRepository: LogCompareRepository,
    val applicationService: ApplicationService,

) {
    val logger: org.slf4j.Logger = LoggerFactory.getLogger(ApplicationService::class.java)

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val restTemplate: RestTemplate = RestTemplateBuilder().build()

    fun getApplicationVersions(
        applicationsIndexes: String,
        user: LogsightUser
    ): MutableList<String> {
        val dataList = mutableListOf<String>()
        try {
            JSONObject(logCompareRepository.getApplicationVersions(applicationsIndexes, user.key))
                .getJSONObject("aggregations")
                .getJSONObject("listAggregations")
                .getJSONArray("buckets").forEach {
                    dataList.add(JSONObject(it.toString()).getString("key"))
                }
        } catch (e: Exception) {
            logger.info("No application indices found.")
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
    ): MutableList<LogCompareTable> {
        val dataList = mutableListOf<LogCompareTable>()
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        val data = JSONObject(
            logCompareRepository.getLogCompareData(
                applicationsIndexes,
                startTime,
                stopTime,
                user.key,
                baselineTagId,
                compareTagId
            )
        )
            .getJSONObject("hits").getJSONArray("hits").forEach {
                val jsonData = JSONObject(it.toString())
                val countPrediction = 1.0 // jsonData.getJSONObject("_source").getDouble("prediction")
                val timestamp = jsonData.getJSONObject("_source").getString("@timestamp")
                val timestampStart = jsonData.getJSONObject("_source").getString("timestamp_start")
                val timestampEnd = jsonData.getJSONObject("_source").getString("timestamp_end")
                val ratioScore = jsonData.getJSONObject("_source").getDouble("ratio_score")
                val numberOfNewAnomalies = jsonData.getJSONObject("_source").getLong("number_of_new_anomalies")
                val numberOfNewNormal = jsonData.getJSONObject("_source").getLong("number_of_new_normal")
                val newTemplates = jsonData.getJSONObject("_source").getJSONArray("new_templates") as JSONArray
                val countAnomalies = JSONArray(jsonData.getJSONObject("_source").getString("message"))
                val countAnomalyList = mutableListOf<VariableAnalysisHitCompareCount>()

                for (i in 0 until countAnomalies.length()) {
                    val newTemplateData = JSONObject(countAnomalies[i].toString())
                    val newTemplate = newTemplateData.getString("template")
                    val appName = newTemplateData.getString("app_name")
                    val newTemplateTimestamp = newTemplateData.getString("@timestamp")
                    var newTemplatePrediction: String = if (newTemplateData.getDouble("prediction") > 0) {
                        "Anomaly"
                    } else {
                        "Normal"
                    }
//                    val newTemplatePrediction = newTemplateData.getDouble("prediction")
                    val newTemplateMessage = newTemplateData.getString("message")
                    val newTemplateActualLevel = newTemplateData.getString("actual_level")
                    val params = mutableListOf<HitParam>()
                    val keys: Iterator<String> = newTemplateData.keys()
//                    val smr = newTemplateData.getDouble("smrs")
//                    val rateNow = newTemplateData.getDouble("rate_now")
                    val smr = 1.0
                    val rateNow = 1.0
                    val tag = newTemplateData.getString("tag")
                    var ratioScoreCount = 0.0
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.startsWith("param_")) {
                            try {
                                params.add(HitParam(key, newTemplateData.getString(key)))
                            } catch (e: JSONException) {
                                continue
                            }
                        }
                    }
                    val templateToGoList = mutableListOf<VariableAnalysisHitCompare>()
                    val templatesToGo = newTemplateData.getJSONArray("templates_to_go")
                    val expectedRates = newTemplateData.getJSONArray("smrs")
                    val ratesNow = newTemplateData.getJSONArray("rate_now")
                    for (j in 0 until templatesToGo.length()) {
                        val item = JSONObject(templatesToGo[j].toString())
                        val hitParams = mutableListOf<HitParam>()
                        val hitKeys: Iterator<String> = item.keys()
                        while (hitKeys.hasNext()) {
                            val key = hitKeys.next()
                            if (key.startsWith("param_")) {
                                hitParams.add(HitParam(key, item.getString(key)))
                            }
                        }
                        var rateNow = 0.0
                        try {
                            rateNow = (ratesNow[j] as BigDecimal).toDouble()
                        } catch (e: ClassCastException) {
                            rateNow = (ratesNow[j] as Int).toDouble()
                        }
                        var expectedRate = 0.0
                        try {
                            expectedRate = (expectedRates[j] as BigDecimal).toDouble()
                        } catch (e: ClassCastException) {
                            expectedRate = (expectedRates[j] as Int).toDouble()
                        }

                        templateToGoList.add(
                            VariableAnalysisHitCompare(
                                message = item.getString("message"),
                                template = item.getString("template"),
                                prediction = if (item.getDouble("prediction") > 0) {
                                    "Anomaly"
                                } else {
                                    "Normal"
                                },
                                params = hitParams,
                                timestamp = item.getString("@timestamp"),
                                actualLevel = item.getString("actual_level"),
                                applicationId = applications[appName]!!,
                                rateNow = roundOffDecimal(rateNow),
                                expectedRate = roundOffDecimal(expectedRate)
                            )
                        )

                        ratioScoreCount += abs(rateNow - expectedRate)
                    }
                    ratioScoreCount /= templatesToGo.length()
                    val variableHit = VariableAnalysisHitCompareCount(
                        newTemplateMessage,
                        newTemplate,
                        newTemplatePrediction,
                        params,
                        newTemplateTimestamp,
                        newTemplateActualLevel,
                        applications[appName]!!,
                        roundOffDecimal(smr),
                        roundOffDecimal(rateNow),
                        tag,
                        templateToGoList,
                        roundOffDecimal(ratioScoreCount)
                    )
                    countAnomalyList.add(variableHit)
                }

                val newTemplateList = mutableListOf<VariableAnalysisHitCompare>()
                for (i in newTemplates) {
                    val newTemplateData = JSONObject(JSONArray(i.toString())[0].toString())
                    val newTemplate = newTemplateData.getString("template")
                    val appName = newTemplateData.getString("app_name")
                    val newTemplateTimestamp = newTemplateData.getString("@timestamp")
                    val newTemplatePrediction: String = if (newTemplateData.getDouble("prediction") > 0) {
                        "Anomaly"
                    } else {
                        "Normal"
                    }
                    val newTemplateMessage = newTemplateData.getString("message")
                    val newTemplateActualLevel = newTemplateData.getString("actual_level")
                    val params = mutableListOf<HitParam>()
                    val keys: Iterator<String> = newTemplateData.keys()
                    val smr = 0.0
                    val rateNow = 1.0
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.startsWith("param_")) {
                            params.add(HitParam(key, newTemplateData.getString(key)))
                        }
                    }
                    val variableHit = VariableAnalysisHitCompare(
                        newTemplateMessage,
                        newTemplate,
                        newTemplatePrediction,
                        params,
                        newTemplateTimestamp,
                        newTemplateActualLevel,
                        applications[appName]!!,
                        roundOffDecimal(smr),
                        roundOffDecimal(rateNow)
                    )
                    newTemplateList.add(variableHit)
                }
                dataList.add(
                    LogCompareTable(
                        applicationId = UtilsService.getApplicationIdFromIndex(
                            applications,
                            jsonData["_index"].toString()
                        ),
                        indexName = jsonData["_index"].toString(),
                        timestamp = timestamp,
                        prediction = countPrediction,
                        timestampStart = timestampStart,
                        timestampEnd = timestampEnd,
                        compareTag = compareTagId,
                        baselineTag = baselineTagId,
                        newTemplateList = newTemplateList,
                        countAnomalyList = countAnomalyList,
                        ratioScore = roundOffDecimal(ratioScore),
                        numberOfNewAnomalies = numberOfNewAnomalies,
                        numberOfNewNormal = numberOfNewNormal
                    )
                )
            }
        return dataList
    }

    fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toDouble()
    }

    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun LocalDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"))
}
