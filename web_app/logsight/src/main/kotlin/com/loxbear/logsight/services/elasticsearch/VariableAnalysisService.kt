package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisSpecificTemplate
import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.repositories.elasticsearch.VariableAnalysisRepository
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class VariableAnalysisService(val repository: VariableAnalysisRepository) {

    fun getTemplates(es_index_user_app: String, startTime: String, stopTime: String, search: String?): List<VariableAnalysisHit> {
        val resp = JSONObject(repository.getTemplates(es_index_user_app, startTime, stopTime, search))
        return resp.getJSONObject("hits").getJSONArray("hits").map {
            val hit = JSONObject(it.toString()).getJSONObject("_source")
            val template = hit.getString("template")
            val message = hit.getString("message")
            val params = mutableListOf<HitParam>();
            val keys: Iterator<String> = hit.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.startsWith("param_")) {
                    params.add(HitParam(key, hit.getString(key)))
                }
            }
            VariableAnalysisHit(message, template, params)
        }
    }

    fun getApplicationIndex(application: Application, key: String): String =
        "${key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${application.name}_parsing"

    fun getSpecificTemplate(es_index_user_app: String, startTime: String, stopTime: String, template: String,
                            param: String, paramValue: String): List<VariableAnalysisSpecificTemplate> {
        val resp = JSONObject(repository.getSpecificTemplate(es_index_user_app, startTime, stopTime, template, param, paramValue))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        return resp.getJSONObject("hits").getJSONArray("hits").mapNotNull {
            val hit = JSONObject(it.toString()).getJSONObject("_source")
            val timestamp = LocalDateTime.parse(hit.getString("@timestamp"), formatter)
            if (hit.has(param))
                VariableAnalysisSpecificTemplate(timestamp, hit.getString(param)) //.replace("[^\\d.]", "")
            else null
        }
    }

    fun getSpecificTemplateGrouped(applicationsIndexes: String, startTime: String, stopTime: String,
                                   template: String, param: String, paramValue: String): Pair<String, List<LineChart>> {
        val specificTemplateData = getSpecificTemplate(applicationsIndexes, startTime, stopTime, template, param, paramValue)
        val allParamsNumbers = checkAllParamsNumbers(specificTemplateData)
        val lineChartData = if (allParamsNumbers) {
            val lineChartSeries = specificTemplateData.mapNotNull {
                try {
                    val d: Double = it.param.toDouble()
                    LineChartSeries(it.timestamp.toString(), d)
                } catch (nfe: NumberFormatException) {
                    null
                }
            }
            listOf(LineChart(name = template, series = lineChartSeries))
        } else {
            getSpecificTemplateDifferentParams(applicationsIndexes, startTime, stopTime, template, param, paramValue)
        }

        return "Grouped" to lineChartData
    }

    private fun getSpecificTemplateDifferentParams(applicationsIndexes: String, startTime: String, stopTime: String,
                                                   template: String, param: String, paramValue: String): List<LineChart> {
        val result = repository.getSpecificTemplateDifferentParams(applicationsIndexes, startTime, stopTime, template, param, paramValue)
            .aggregations.listAggregations.buckets.map {
                val name = it.date.toString()
                val series = it.listBuckets.buckets.map { it2 ->
                    LineChartSeries(name = it2.key, value = it2.docCount)
                }
                LineChart(name, series)
            }

        return result
    }

    private fun checkAllParamsNumbers(specificTemplateData: List<VariableAnalysisSpecificTemplate>): Boolean {
        specificTemplateData.forEach {
            try {
                val d: Double = it.param.toDouble()
            } catch (nfe: NumberFormatException) {
                return false
            }
        }
        return true
    }

    fun ZonedDateTime.toBookingTime(): String = this.format(DateTimeFormatter.ofPattern("HH:mm"))

}

