package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.IncidentTimeline
import com.loxbear.logsight.charts.data.IncidentTimelineData
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.charts.data.TopKIncidentTable
import com.loxbear.logsight.repositories.elasticsearch.IncidentRepository
import org.json.JSONObject

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
class IncidentService(val repository: IncidentRepository) {


    fun getTopKIncidentsTableData(esIndexUserApp: String, startTime: String, stopTime: String): List<TopKIncidentTable> {

        val dataList = mutableListOf<TopKIncidentTable>()

       JSONObject(repository.getTopKIncidentData(esIndexUserApp, startTime, stopTime)).getJSONObject("hits").
       getJSONArray("hits").forEach {
           val jsonData = JSONObject(it.toString())
           dataList.add(TopKIncidentTable(indexName = jsonData["_index"].toString(),
                   startTimestamp = jsonData.getJSONObject("_source")["timestamp_start"].toString(),
                   stopTimestamp = jsonData.getJSONObject("_source")["timestamp_end"].toString(),
                   firstLog = "",// jsonData.getJSONObject("_source")["first_log"].toString()
                   lastLog = "", // jsonData.getJSONObject("_source")["first_log"].toString()
                   totalScore = jsonData.getJSONObject("_source")["severity_score"] as BigDecimal
                   ))
       }
        dataList.sortByDescending {it.totalScore}
        return dataList
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

    fun getIncidentsTableData(applicationsIndexes: String, startTime: String, stopTime: String) {
        val resp = JSONObject(repository.getIncidentsTableData(applicationsIndexes, startTime, stopTime))
        println(resp)
    }


    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm"))
}

