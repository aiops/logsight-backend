package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.*
import com.loxbear.logsight.repositories.elasticsearch.IncidentRepository
import org.json.JSONObject
import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.services.ApplicationService
import org.json.JSONArray

import org.springframework.stereotype.Service
import utils.UtilsService
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
class IncidentService(val repository: IncidentRepository, val applicationService: ApplicationService) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

    fun getTopKIncidentsTableData(
        esIndexUserApp: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        numberOfIncidents: Int
    ): List<TopKIncidentTable> {
        val applications = applicationService.findAllByUser(user).associate { it.name to it.id }
        val dataList = mutableListOf<TopKIncidentTable>()

        JSONObject(repository.getTopKIncidentData(esIndexUserApp, startTime, stopTime, user.key)).getJSONObject("hits")
            .getJSONArray("hits").forEach {
            val jsonData = JSONObject(it.toString())
            dataList.add(
                TopKIncidentTable(
                    applicationId = UtilsService.getApplicationIdFromIndex(applications, jsonData["_index"].toString()),
                    indexName = jsonData["_index"].toString(),
                    timestamp = jsonData.getJSONObject("_source")["@timestamp"].toString(),
                    startTimestamp = jsonData.getJSONObject("_source")["timestamp_start"].toString(),
                    stopTimestamp = jsonData.getJSONObject("_source")["timestamp_end"].toString(),
                    newTemplates = jsonData.getJSONObject("_source")["new_templates"].toString(),// jsonData.getJSONObject("_source")["first_log"].toString()
                    semanticAD = jsonData.getJSONObject("_source")["semantic_ad"].toString(), // jsonData.getJSONObject("_source")["first_log"].toString()
                    countAD = jsonData.getJSONObject("_source")["count_ads"].toString(),
                    scAnomalies = jsonData.getJSONObject("_source")["semantic_count_ads"].toString(),
                    totalScore = jsonData.getJSONObject("_source")["total_score"] as Int
                )
            )
        }
        dataList.sortByDescending { it.totalScore }
        return if (dataList.size <= numberOfIncidents) {
            dataList
        } else {
            dataList.subList(0, numberOfIncidents)
        }

    }

    fun getIncidentsBarChartData(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        intervalAggregate: String,
        user: LogsightUser
    ): List<LineChartSeries> {
        val resp = JSONObject(repository.getIncidentsBarChartData(applicationsIndexes, startTime, stopTime, intervalAggregate, user.key))
        return resp.getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").map {
            val obj = JSONObject(it.toString())
            val date = ZonedDateTime.parse(obj.getString("key_as_string"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            LineChartSeries(date.toDateTime(), obj.getDouble("doc_count"))
        }
    }

    fun getIncidentsTableData(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        intervalAggregate: String,
        user: LogsightUser
    ): IncidentTableData {
        val anomalies = listOf("count_ads", "semantic_count_ads", "new_templates", "semantic_ad")
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        return JSONObject(repository.getIncidentsTableData(applicationsIndexes, startTime, stopTime, intervalAggregate, user.key))
            .getJSONObject("hits").getJSONArray("hits").fold(IncidentTableData()) { acc, it ->
                val app_name = JSONObject(it.toString()).getString("_index").split("_")
                    .subList(1, JSONObject(it.toString()).getString("_index").split("_").size - 1).joinToString("_")
                val tableData = JSONObject(it.toString()).getJSONObject("_source")
                val incidentTableData = anomalies.mapIndexed { index: Int, anomaly: String ->
                    if (tableData.has(anomaly)) {
                        val hit = tableData.getJSONArray(anomalies[index])
                        if (!hit.isEmpty && hit[0].toString().isNotEmpty()) {
                            val list = JSONArray(hit.toString()).map { one ->
                                val data = JSONObject(JSONArray(one.toString())[0].toString())
                                val template = data.getString("template")
                                val message = data.getString("message")
                                val timestampTmp = data.getString("@timestamp")
                                val timeStamp = LocalDateTime.parse(timestampTmp, formatter).toDateTime()
                                val actualLevel = data.getString("actual_level")
                                val params = mutableListOf<HitParam>()
                                val keys: Iterator<String> = data.keys()
                                var smr = 0.0
                                var rate_now = 1.0
                                try {
                                    smr = data.getDouble("smrs")
                                    rate_now = data.getDouble("rate_now")
                                } catch (e: Exception) {
                                }
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    if (key.startsWith("param_")) {
                                        params.add(HitParam(key, data.getString(key)))
                                    }
                                }
                                anomalies[index] to VariableAnalysisHit(
                                    message,
                                    template,
                                    params,
                                    timeStamp,
                                    actualLevel,
                                    applications[app_name]!!,
                                    smr,
                                    rate_now
                                )
                            }
                            list
                        } else {
                            listOf()
                        }
                    } else {
                        listOf()
                    }
                }.flatten().groupBy({ it.first }, { it.second })

                val countAds =
                    if (incidentTableData["count_ads"] != null) incidentTableData["count_ads"]!! else listOf<VariableAnalysisHit>() // flow anomalies
                val semanticCountAds =
                    if (incidentTableData["semantic_count_ads"] != null) incidentTableData["semantic_count_ads"]!! else listOf<VariableAnalysisHit>() // critical
                val newTemplates =
                    if (incidentTableData["new_templates"] != null) incidentTableData["new_templates"]!! else listOf<VariableAnalysisHit>() // new log types
                val semanticAd =
                    if (incidentTableData["semantic_ad"] != null) incidentTableData["semantic_ad"]!! else listOf<VariableAnalysisHit>() //cognitive anomalies
                IncidentTableData(
                    count_ads = acc.countAds + countAds,
                    semantic_count_ads = acc.semanticCountAds + semanticCountAds,
                    new_templates = acc.newTemplates + newTemplates,
                    semantic_ad = acc.semanticAd + semanticAd,
                )
            }
    }

    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun LocalDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"))
}

