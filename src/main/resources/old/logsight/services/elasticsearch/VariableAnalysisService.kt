package com.loxbear.logsight.services.elasticsearch

import ai.logsight.backend.charts.domain.charts.LineChart
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.charts.elasticsearch.HitParam
import ai.logsight.backend.charts.repository.entities.elasticsearch.ValueResultBucket
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisSpecificTemplate
import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.TopNTemplatesData
import com.loxbear.logsight.repositories.elasticsearch.VariableAnalysisRepository
import com.loxbear.logsight.services.ApplicationService
import org.json.JSONObject
import org.springframework.stereotype.Service
import utils.UtilsService
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@Service
class VariableAnalysisService(
    val repository: VariableAnalysisRepository,
    val applicationService: ApplicationService
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

    fun getTemplates(
        es_index_user_app: String,
        startTime: String,
        stopTime: String,
        intervalAggregate: String,
        search: String?,
        user: LogsightUser
    ): List<VariableAnalysisHit> {
        val resp = JSONObject(
            repository.getTemplates(
                es_index_user_app,
                startTime,
                stopTime,
                intervalAggregate,
                search,
                user.key
            )
        )
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        val app_name = es_index_user_app.split("_").subList(1, es_index_user_app.split("_").size - 2).joinToString("_")
        return resp.getJSONObject("hits").getJSONArray("hits").map {
            val hit = JSONObject(it.toString()).getJSONObject("_source")
            val template = hit.getString("template")
            val message = hit.getString("message")
            val timeStamp = LocalDateTime.parse(hit.getString("@timestamp"), formatter).toDateTime()
            val actualLevel = hit.getString("actual_level")
            val params = mutableListOf<HitParam>();
            val keys: Iterator<String> = hit.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.startsWith("param_")) {
                    params.add(HitParam(key, hit.getString(key)))
                }
            }
            VariableAnalysisHit(message, template, params, timeStamp, actualLevel, applications[app_name]!!, 0.0, 1.0)
        }
    }

    //    fun getApplicationIndex(application: Application, key: String): String =
//        "${key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${application.name}_parsing"
    fun getApplicationIndex(application: Application, key: String): String =
        "${key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${application.name}_log_ad"

    fun getSpecificTemplate(
        es_index_user_app: String, startTime: String, stopTime: String, intervalAggregate: String, template: String,
        param: String, paramValue: String, userKey: String
    ): List<VariableAnalysisSpecificTemplate> {
        val resp = JSONObject(
            repository.getSpecificTemplate(
                es_index_user_app,
                startTime,
                stopTime,
                intervalAggregate,
                template,
                param,
                paramValue,
                userKey
            )
        )
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        return resp.getJSONObject("hits").getJSONArray("hits").mapNotNull {
            val hit = JSONObject(it.toString()).getJSONObject("_source")
            val timestamp = LocalDateTime.parse(hit.getString("@timestamp"), formatter)
            if (hit.has(param))
                VariableAnalysisSpecificTemplate(timestamp, hit.getString(param)) //.replace("[^\\d.]", "")
            else null
        }
    }

    fun getSpecificTemplateGrouped(
        applicationsIndexes: String, startTime: String, stopTime: String, intervalAggregate: String,
        template: String, param: String, paramValue: String, user: LogsightUser
    ): Pair<String, List<LineChart>> {
        val specificTemplateData =
            getSpecificTemplate(
                applicationsIndexes,
                startTime,
                stopTime,
                intervalAggregate,
                template,
                param,
                paramValue,
                user.key
            )
        val allParamsNumbers = checkAllParamsNumbers(specificTemplateData)
        return if (allParamsNumbers) {
            val lineChartSeries = specificTemplateData.mapNotNull {
                try {
                    val parsed = UtilsService.getLeadingNumber(it.param)
                    LineChartSeries(name = it.timestamp.toDateTime(), value = parsed.toDouble())
                } catch (nfe: NumberFormatException) {
                    null
                }
            }
            "LineChart" to listOf(LineChart(name = "value", series = lineChartSeries))
        } else {
            "GroupedVertical" to getSpecificTemplateDifferentParams(
                applicationsIndexes,
                startTime,
                stopTime,
                template,
                param,
                paramValue,
                user
            )
        }
    }

    private fun getSpecificTemplateDifferentParams(
        applicationsIndexes: String, startTime: String, stopTime: String,
        template: String, param: String, paramValue: String, user: LogsightUser
    ): List<LineChart> {
        return repository.getSpecificTemplateDifferentParams(
            applicationsIndexes,
            startTime,
            stopTime,
            template,
            param,
            paramValue,
            user.key
        )
            .aggregations.listAggregations.buckets.map {
                val name = it.date.toDateTime()
                val series = it.listBuckets.buckets.map { it2 ->
                    LineChartSeries(name = it2.key, value = it2.docCount)
                }
                LineChart(name, series)
            }
    }

    private fun checkAllParamsNumbers(specificTemplateData: List<VariableAnalysisSpecificTemplate>): Boolean {
        specificTemplateData.forEach {
            return try {
                val parsed = UtilsService.getLeadingNumber(it.param)
                it.param.length - parsed.length < 3
            } catch (nfe: NumberFormatException) {
                false
            }
        }
        return true
    }

    fun getTopNTemplates(applicationsIndexes: String, user: LogsightUser, startTime: String, endTime: String): Map<String, List<TopNTemplatesData>> {
        val top5TemplatesNow = repository.getTopNTemplates(
            applicationsIndexes,
            "now-1h",
            "now",
            5,
            user.key
        ).aggregations.listAggregations.buckets
        val top5TemplatesNowNames = top5TemplatesNow.map { it.key }
        val top5TemplatesOlder = repository.getTopNTemplates(
            applicationsIndexes,
            "now-2h",
            "now-1h",
            20,
            user.key
        ).aggregations.listAggregations.buckets
            .filter { top5TemplatesNowNames.contains(it.key) }

        return calculateTopTemplatesDifference(top5TemplatesNow, top5TemplatesOlder)
    }

    private fun calculateTopTemplatesDifference(
        top5TemplatesNow: List<ValueResultBucket>,
        top5TemplatesOlder: List<ValueResultBucket>
    ): Map<String, List<TopNTemplatesData>> {
        val result: MutableMap<String, List<TopNTemplatesData>> = HashMap()
        val top5TemplatesOlderMap = top5TemplatesOlder.map { it.key to it }.toMap()
        val top5TemplatesNowData = top5TemplatesNow.map {
            val percentageDifference =
                if (top5TemplatesOlderMap.containsKey(it.key)) (abs(it.docCount - top5TemplatesOlderMap[it.key]!!.docCount) / top5TemplatesOlderMap[it.key]!!.docCount) * 100 else 0.0
            TopNTemplatesData(
                template = it.key,
                count = it.docCount,
                percentage = String.format("%.1f", percentageDifference).toDouble()
            )
        }
        result["now"] = top5TemplatesNowData
        val top5TemplatesOlderData = top5TemplatesOlder.map {
            TopNTemplatesData(template = it.key, count = it.docCount)
        }
        result["older"] = top5TemplatesOlderData
        return result
    }

    fun getLogCountLineChart(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser,
        intervalAggregate: String
    ): List<LineChart> {
        val resp = JSONObject(repository.getLogCountLineChart(applicationsIndexes, startTime, stopTime, user.key, intervalAggregate))
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

    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"))
    fun LocalDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"))
    fun ZonedDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    fun LocalDateTime.toHourMinute(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
}

