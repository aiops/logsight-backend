package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.*
import com.loxbear.logsight.repositories.elasticsearch.IncidentRepository
import org.json.JSONObject
import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import org.json.JSONArray

import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess


@Service
class IncidentService(val repository: IncidentRepository) {

    fun getTopKIncidentsTableData(esIndexUserApp: String, startTime: String, stopTime: String): List<TopKIncidentTable> {

        val dataList = mutableListOf<TopKIncidentTable>()

        JSONObject(repository.getTopKIncidentData(esIndexUserApp, startTime, stopTime)).getJSONObject("hits").getJSONArray("hits").forEach {
            val jsonData = JSONObject(it.toString())
            dataList.add(TopKIncidentTable(indexName = jsonData["_index"].toString(),
                timestamp = jsonData.getJSONObject("_source")["@timestamp"].toString(),
                startTimestamp = jsonData.getJSONObject("_source")["timestamp_start"].toString(),
                stopTimestamp = jsonData.getJSONObject("_source")["timestamp_end"].toString(),
                newTemplates = jsonData.getJSONObject("_source")["new_templates"].toString(),// jsonData.getJSONObject("_source")["first_log"].toString()
                semanticAD = jsonData.getJSONObject("_source")["semantic_ad"].toString(), // jsonData.getJSONObject("_source")["first_log"].toString()
                countAD = jsonData.getJSONObject("_source")["count_ads"].toString(),
                scAnomalies = jsonData.getJSONObject("_source")["semantic_count_ads"].toString(),
                totalScore = jsonData.getJSONObject("_source")["total_score"] as Int
            ))
        }
        dataList.sortByDescending { it.totalScore }
        return if (dataList.size <= 5) {
            dataList
        } else {
            dataList.subList(0, 5)
        }

    }

    fun getIncidentsBarChartData(applicationsIndexes: String, startTime: String, stopTime: String): List<LineChartSeries> {
        val resp = JSONObject(repository.getIncidentsBarChartData(applicationsIndexes, startTime, stopTime))
        return resp.getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").map {
            val obj = JSONObject(it.toString())
            val date = ZonedDateTime.parse(obj.getString("key_as_string"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            LineChartSeries(date.toDateTime(), obj.getDouble("doc_count"))
        }
    }

    fun getIncidentsTableData(applicationsIndexes: String, startTime: String, stopTime: String): IncidentTableData {
        val anomalies = listOf("count_ads", "semantic_count_ads", "new_templates", "semantic_ad")

        return JSONObject(repository.getIncidentsTableData(applicationsIndexes, startTime, stopTime))
            .getJSONObject("hits").getJSONArray("hits").fold(IncidentTableData(), { acc, it ->
                val tableData = JSONObject(it.toString()).getJSONObject("_source")
                val incidentTableData = anomalies.mapIndexed { index: Int, anomaly: String ->
                    if (tableData.has(anomaly)) {
                        val hit = tableData.getJSONArray(anomalies[index])
                        if (!hit.isEmpty && hit[0].toString().isNotEmpty()) {
                            val list = JSONArray(hit.toString()).map { one ->
                                val data = JSONObject(JSONArray(one.toString())[0].toString())
                                val template = data.getString("template")
                                val message = data.getString("message")
                                val params = mutableListOf<HitParam>()
                                val keys: Iterator<String> = data.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    if (key.startsWith("param_")) {
                                        params.add(HitParam(key, data.getString(key)))
                                    }
                                }
                                anomalies[index] to VariableAnalysisHit(message, template, params)
                            }
                            list
                        } else {
                            listOf()
                        }
                    } else {
                        listOf()
                    }
                }.flatten().groupBy({ it.first }, { it.second })

                val countAds = if (incidentTableData["count_ads"] != null) incidentTableData["count_ads"]!! else listOf<VariableAnalysisHit>() // flow anomalies
                val semanticCountAds = if (incidentTableData["semantic_count_ads"] != null) incidentTableData["semantic_count_ads"]!! else listOf<VariableAnalysisHit>() // critical
                val newTemplates = if (incidentTableData["new_templates"] != null) incidentTableData["new_templates"]!! else listOf<VariableAnalysisHit>() // new log types
                val semanticAd = if (incidentTableData["semantic_ad"] != null) incidentTableData["semantic_ad"]!! else listOf<VariableAnalysisHit>() //cognitive anomalies
                IncidentTableData(
                    count_ads = acc.countAds + countAds,
                    semantic_count_ads = acc.semanticCountAds + semanticCountAds,
                    new_templates = acc.newTemplates + newTemplates,
                    semantic_ad = acc.semanticAd + semanticAd,
                )
            })
    }

    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
}

