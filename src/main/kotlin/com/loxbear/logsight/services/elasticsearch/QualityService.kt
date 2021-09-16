package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.charts.elasticsearch.HitParam
import com.loxbear.logsight.charts.elasticsearch.LogQualityOverview
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import org.json.JSONObject
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LogQualityTable
import com.loxbear.logsight.repositories.elasticsearch.QualityRepository
import com.loxbear.logsight.services.ApplicationService
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
class QualityService(val repository: QualityRepository, val applicationService: ApplicationService) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val restTemplate = RestTemplateBuilder()
        .build();

    @Value("\${app.baseUrl}")
    private val baseUrl: String? = null

    fun getLogQualityData(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser
    ): MutableList<LogQualityTable> {
        val applications = applicationService.findAllByUser(user).map { it.name to it.id }.toMap()
        val dataList = mutableListOf<LogQualityTable>()
        val jsonData = JSONObject(repository.getLogQualityData(applicationsIndexes, startTime, stopTime, user.key))
            .getJSONObject("hits")
            .getJSONArray("hits").forEach {
                val jsonData = JSONObject(it.toString())
                val linguisticPrediction = jsonData.getJSONObject("_source")["linguistic_prediction"] as BigDecimal
                val actualLevel = jsonData.getJSONObject("_source")["actual_level"].toString()
                var predictedLevel: String = if (jsonData.getJSONObject("_source")["predicted_log_level"].toString() == "0"){
                    "ERROR or WARNING"
                }else{
                    "INFO or DEBUG"
                }
                val suggestions = jsonData.getJSONObject("_source").getJSONArray("suggestions")[0] as JSONArray
                val suggestionData = mutableListOf<String>()
                for (i in suggestions){
                    suggestionData.add(i as String)
                }

                val tags = jsonData.getJSONObject("_source").getJSONArray("suggestions")[1]
                val appName = jsonData.getJSONObject("_source")["app_name"].toString()
                val template = jsonData.getJSONObject("_source")["template"].toString()
                val message = jsonData.getJSONObject("_source")["message"].toString()
                val timestampTmp = jsonData.getJSONObject("_source")["@timestamp"].toString()
                val timeStamp = LocalDateTime.parse(timestampTmp, formatter).toDateTime()
                val params = mutableListOf<HitParam>()
                val keys: Iterator<String> = jsonData.getJSONObject("_source").keys()
                val smr = 0.0
                val rateNow = 1.0
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key.startsWith("param_")) {
                        params.add(HitParam(key, jsonData.getJSONObject("_source").getString(key)))
                    }
                }
                val variableHit = VariableAnalysisHit(
                    message,
                    template,
                    params,
                    timeStamp,
                    actualLevel,
                    applications[appName]!!,
                    smr,
                    rateNow
                )
                dataList.add(
                    LogQualityTable(
                        applicationId = UtilsService.getApplicationIdFromIndex(applications, jsonData["_index"].toString()),
                        indexName = jsonData["_index"].toString(),
                        timestamp = timeStamp,
                        template = template,
                        appName = appName,
                        message = message,
                        predictedLevel = predictedLevel,
                        actualLevel = actualLevel,
                        linguisticPrediction = linguisticPrediction.round(MathContext(3)),
                        suggestions = suggestionData,
                        tags = tags as String,
                        variableHit = variableHit
                    )
                )
            }
        return dataList
    }


    fun getLogQualityOverview(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser
    ): MutableList<LogQualityOverview> {
        val dataList = mutableListOf<LogQualityOverview>()
        JSONObject(repository.getLogQualityOverview(applicationsIndexes, startTime, stopTime, user.key))
            .getJSONObject("aggregations").getJSONObject("listAggregations").getJSONArray("buckets").forEach {
                val itData = JSONObject(it.toString())
                dataList.add(LogQualityOverview(key = itData.getString("key"),
                    docCount = itData.getDouble("doc_count"),
                    linguisticPrediction = roundOffDecimal(itData.getJSONObject("linguisticPrediction").getDouble("value")),
                    logLevelScore = roundOffDecimal(itData.getJSONObject("logLevelScore").getDouble("value")) ))
            }

        return dataList
    }

    fun computeLogQuality(
        applicationsIndexes: String,
        startTime: String,
        stopTime: String,
        user: LogsightUser
    ): HttpStatus {
        for (i in applicationsIndexes.split(',')){
            val request = "{\n" +
                    "  \"private-key\": \"${user.key}\",\n" +
                    "  \"app\": \"${i.split("_")[1]}\",\n" +
                    "  \"start-time\": \"$startTime\",\n" +
                    "  \"end-time\": \"$stopTime\",\n" +
                    "  \"anomaly-type\": \"log_quality\"\n" +
                    "}"
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val json = JSONObject(request)

            try {
                restTemplate.postForEntity<String>(
                    "http://wally113.cit.tu-berlin.de:5444/api_v1/results", HttpEntity(json.toString(), headers)).body!!
            }catch (e: HttpClientErrorException){
                return HttpStatus.BAD_REQUEST
            }

        }
        return HttpStatus.OK
    }

    fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    fun ZonedDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
    fun LocalDateTime.toDateTime(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"))
}

