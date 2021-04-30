package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.IncidentTimeline
import com.loxbear.logsight.charts.data.IncidentTimelineData
import com.loxbear.logsight.charts.data.TopKIncidentTable
import com.loxbear.logsight.repositories.elasticsearch.IncidentRepository
import org.json.JSONObject
import com.google.gson.*
import com.loxbear.logsight.charts.data.IncidentTableData
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
class IncidentService(val repository: IncidentRepository) {

    val gson = Gson()

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
        return if(dataList.size <= 5){
            dataList
        }else{
            dataList.subList(0,5)
        }

    }

    fun getIncidentsBarChartData(applicationsIndexes: String, startTime: String, stopTime: String): List<IncidentTimelineData> {
        val resp = JSONObject(repository.getIncidentsBarChartData(applicationsIndexes, startTime, stopTime))
        val incidentTimeline = resp.getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").map {
            val obj = JSONObject(it.toString())
            val date = ZonedDateTime.parse(obj.getString("key_as_string"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            IncidentTimeline(date.toInstant().toEpochMilli(), obj.getDouble("doc_count"))
        }
        return listOf(IncidentTimelineData(key = "data", values = incidentTimeline))
    }

    fun getIncidentsTableData(applicationsIndexes: String, startTime: String, stopTime: String): IncidentTableData {
        return JSONObject(repository.getIncidentsTableData(applicationsIndexes, startTime, stopTime))
            .getJSONObject("hits").getJSONArray("hits").fold(IncidentTableData(), { acc, it ->
                val tableData = gson.fromJson((it as JSONObject).getJSONObject("_source").toString(), IncidentTableData::class.java)
                IncidentTableData(
                    count_ads = acc.countAds + tableData.countAds.filter { it.isNotEmpty() }
                        .fold(listOf<String>(), { previous, next -> previous + next.split("||") }),
                    semantic_count_ads = acc.semanticCountAds + tableData.semanticCountAds.filter { it.isNotEmpty() }
                        .fold(listOf<String>(), { previous, next -> previous + next.split("||") }),
                    new_templates = acc.newTemplates + tableData.newTemplates.filter { it.isNotEmpty() }
                        .fold(listOf<String>(), { previous, next -> previous + next.split("||") }),
                    semantic_ad = acc.semanticAd + tableData.semanticAd.filter { it.isNotEmpty() }
                        .fold(listOf<String>(), { previous, next -> previous + next.split("||") }),
                )
            })
    }


    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm"))
}

