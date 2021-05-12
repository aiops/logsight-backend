package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.ResultBucket
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisSpecificTemplate
import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.models.TopNTemplatesData
import com.loxbear.logsight.repositories.elasticsearch.VariableAnalysisRepository
import org.json.JSONObject
import org.springframework.stereotype.Service
import utils.UtilsService
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

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
        return if (allParamsNumbers) {
            val lineChartSeries = specificTemplateData.mapNotNull {
                try {
                    val parsed = UtilsService.getLeadingNumber(it.param)
                    LineChartSeries(name = it.timestamp.toHourMinute(), value = parsed.toDouble())
                } catch (nfe: NumberFormatException) {
                    null
                }
            }
            "LineChart" to listOf(LineChart(name = template, series = lineChartSeries))
        } else {
            "GroupedVertical" to getSpecificTemplateDifferentParams(applicationsIndexes, startTime, stopTime, template, param, paramValue)
        }
    }

    private fun getSpecificTemplateDifferentParams(applicationsIndexes: String, startTime: String, stopTime: String,
                                                   template: String, param: String, paramValue: String): List<LineChart> {
        return repository.getSpecificTemplateDifferentParams(applicationsIndexes, startTime, stopTime, template, param, paramValue)
            .aggregations.listAggregations.buckets.map {
                val name = it.date.toHourMinute()
                val series = it.listBuckets.buckets.map { it2 ->
                    LineChartSeries(name = it2.key, value = it2.docCount)
                }
                LineChart(name, series)
            }
    }

    private fun checkAllParamsNumbers(specificTemplateData: List<VariableAnalysisSpecificTemplate>): Boolean {
        specificTemplateData.forEach {
            try {
                val parsed = UtilsService.getLeadingNumber(it.param)
                val d: Double = parsed.toDouble()
            } catch (nfe: NumberFormatException) {
                return false
            }
        }
        return true
    }

    fun getTopNTemplates(applicationsIndexes: String): Map<String, List<TopNTemplatesData>> {
        val top5TemplatesNow = repository.getTopNTemplates(applicationsIndexes, "now-1h", "now", 5).aggregations.listAggregations.buckets
        val top5TemplatesNowNames = top5TemplatesNow.map { it.key }
        val top5TemplatesOlder = repository.getTopNTemplates(applicationsIndexes, "now-2h", "now-1h", 20).aggregations.listAggregations.buckets
            .filter { top5TemplatesNowNames.contains(it.key) }

        return calculateTopTemplatesDifference(top5TemplatesNow, top5TemplatesOlder)
    }

    private fun calculateTopTemplatesDifference(top5TemplatesNow: List<ResultBucket>, top5TemplatesOlder: List<ResultBucket>): Map<String, List<TopNTemplatesData>> {
        val result: MutableMap<String, List<TopNTemplatesData>> = HashMap()
        val top5TemplatesOlderMap = top5TemplatesOlder.map { it.key to it }.toMap()
        val top5TemplatesNowData = top5TemplatesNow.map {
            val percentageDifference = if (top5TemplatesOlderMap.containsKey(it.key)) (top5TemplatesOlderMap[it.key]!!.docCount / it.docCount) * 100 else 0.0
            TopNTemplatesData(template = it.key, count = it.docCount, percentage = String.format("%.1f", percentageDifference).toDouble())
        }
        result["now"] = top5TemplatesNowData
        val top5TemplatesOlderData = top5TemplatesOlder.map {
            TopNTemplatesData(template = it.key, count = it.docCount)
        }
        result["older"] = top5TemplatesOlderData
        return result
    }

    fun getLogCountLineChart(applicationsIndexes: String, startTime: String, stopTime: String): List<LineChart> {
        val resp = JSONObject(repository.getLogCountLineChart(applicationsIndexes, startTime, stopTime))
        val lineChartSeries = resp.getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").map {
            val obj = JSONObject(it.toString())
            val date = ZonedDateTime.parse(obj.getString("key_as_string"), ISO_DATE_TIME).toDateTime()
            LineChartSeries(date.split('+')[0], obj.getDouble("doc_count"))
        }
        return listOf(LineChart("Log Count", lineChartSeries))
    }
    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    fun LocalDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))

}

