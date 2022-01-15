package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.models.LogCompareTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.UserService
import com.loxbear.logsight.services.elasticsearch.LogCompareService
import org.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import utils.UtilsService
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@RestController
@RequestMapping("/api/log_compare")
class LogCompareController(
    val logCompareService: LogCompareService,
    val userService: UserService,
    val applicationService: ApplicationService,
    val kafkaService: KafkaService
) {

    @GetMapping("/load_versions")
    fun getVersions(
        authentication: Authentication,
        @RequestParam(required = true) applicationId: Long?
    ): MutableList<String> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes =
            applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "log_ad")
        val applicationVersions = logCompareService.getApplicationVersions(applicationsIndexes, user)
        applicationVersions
    }.orElse(mutableListOf())

    @GetMapping("/compute_log_compare")
    fun trainLogCompareModel(
        authentication: Authentication,
        @RequestParam(required = true) applicationId: Long?,
        @RequestParam baselineTag: String,
        @RequestParam compareTag: String,
        @RequestParam selectedTime: String // this should be forwarded to the backend
    ): String {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val client = HttpClient.newBuilder().build()
        val uriComponents = application?.get()?.let {
            UriComponentsBuilder.newInstance().scheme("http")
                .host("localhost")
                .port(5000)
                .path("api/compute_log_compare")
                .queryParam("applicationName", it.name)
                .queryParam("baselineTag", baselineTag)
                .queryParam("compareTag", compareTag)
                .queryParam("privateKey", user.get().key).build()
        }
        if (uriComponents != null) {
            print(uriComponents.toUriString())
        }
        val request = HttpRequest.newBuilder()
            .uri(uriComponents?.toUri())
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return JSONObject(response.body()).toString()
    }

    @GetMapping("/bar_plot_count")
    fun getLogCountBar(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long,
        @RequestParam(required = false) tag: String
    ): List<LineChart> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationService.findById(applicationId)
        val applicationsIndexes =
            applicationService.getApplicationIndexesForLogCompare(user, application.orElse(null), "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        logCompareService.getLogCountBar(applicationsIndexes, startTime, endTime, user, intervalAggregate, tag)
    }.orElse(listOf())

    @GetMapping("/cognitive_bar_plot")
    fun getAnomaliesBarChartData(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long,
        @RequestParam(required = false) tag: String
    ): List<LineChart> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationService.findById(applicationId)
        val applicationsIndexes =
            applicationService.getApplicationIndexesForLogCompare(user, application.orElse(null), "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        logCompareService.getAnomaliesBarChartData(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            intervalAggregate,
            tag
        )
    }.orElse(listOf())

    @GetMapping("/compare_templates_horizontal_bar")
    fun getCompareTemplatesHorizontalBar(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam baselineTagId: String,
        @RequestParam compareTagId: String,
        @RequestParam(required = false) applicationId: Long
    ): List<LineChart> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationService.findById(applicationId)
        val applicationsIndexes =
            applicationService.getApplicationIndexesForLogCompare(user, application.orElse(null), "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        logCompareService.getCompareTemplatesHorizontalBar(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            intervalAggregate,
            baselineTagId,
            compareTagId
        )
    }.orElse(listOf())

    @GetMapping("/data")
    fun getLogCompareData(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long?,
        @RequestParam baselineTagId: String,
        @RequestParam compareTagId: String
    ): MutableList<LogCompareTable> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes =
            applicationService.getApplicationIndexesForLogCompare(user, application?.orElse(null), "count_ad")
        logCompareService.getLogCompareData(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            baselineTagId,
            compareTagId
        )
    }.orElse(mutableListOf())
}
