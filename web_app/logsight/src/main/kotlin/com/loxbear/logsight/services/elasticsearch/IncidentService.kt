package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.incidents.data.TopKIncidentTable
import com.loxbear.logsight.repositories.elasticsearch.IncidentRepository
import org.json.JSONObject

import org.springframework.stereotype.Service
import java.math.BigDecimal


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
                   firstLog = jsonData.getJSONObject("_source")["first_log"].toString(),
                   lastLog = jsonData.getJSONObject("_source")["first_log"].toString(),
                   totalScore = jsonData.getJSONObject("_source")["total_score"] as BigDecimal
                   ))
       }
        dataList.sortByDescending {it.totalScore}
        return dataList
    }
}

