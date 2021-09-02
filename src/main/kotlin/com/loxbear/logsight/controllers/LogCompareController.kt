package com.loxbear.logsight.controllers
import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.models.LogCompareTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.UserService
import com.loxbear.logsight.services.elasticsearch.LogCompareService
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import utils.UtilsService


@RestController
@RequestMapping("/api/log_compare")
class LogCompareController(val logCompareService: LogCompareService, val userService: UserService,
                        val applicationService: ApplicationService, val kafkaService: KafkaService) {

    val restTemplate = RestTemplateBuilder()
        .build();

    @GetMapping("/load_versions")
    fun getVersions(authentication: Authentication,
                          @RequestParam(required = true) applicationId: Long?): MutableList<String> {

        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "log_ad")
        val applicationVersions = logCompareService.getApplicationVersions(applicationsIndexes, user)
        return applicationVersions
    }

    @GetMapping("/compute_log_compare")
    fun trainLogCompareModel(authentication: Authentication,
                          @RequestParam(required = true) applicationId: Long?,
                          @RequestParam baselineTagId: String,
                          @RequestParam compareTagId: String
                          ) {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
//        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application)
        if (application != null) {
            kafkaService.trainModels(user, application, baselineTagId, compareTagId)
        }
    }

    @GetMapping("/bar_plot_count")
    fun getLogCountBar(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long,
        @RequestParam(required = false) tag: String
    ): List<LineChart> {
        val user = userService.findByEmail(authentication.name)
        val application = applicationService.findById(applicationId)
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        return logCompareService.getLogCountBar(applicationsIndexes, startTime, endTime, user, intervalAggregate, tag)
    }

    @GetMapping("/cognitive_bar_plot")
    fun getAnomaliesBarChartData(authentication: Authentication,
                                 @RequestParam startTime: String,
                                 @RequestParam endTime: String,
                                 @RequestParam(required = false) applicationId: Long,
                                 @RequestParam(required = false) tag: String): List<LineChart> {
        val user = userService.findByEmail(authentication.name)
        val application = applicationService.findById(applicationId)
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        return logCompareService.getAnomaliesBarChartData(applicationsIndexes, startTime, endTime, user, intervalAggregate, tag)
    }


    @GetMapping("/compare_templates_horizontal_bar")
    fun getCompareTemplatesHorizontalBar(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam baselineTagId: String,
        @RequestParam compareTagId: String,
        @RequestParam(required = false) applicationId: Long
    ): List<LineChart> {
        val user = userService.findByEmail(authentication.name)
        val application = applicationService.findById(applicationId)
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "log_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        return logCompareService.getCompareTemplatesHorizontalBar(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            intervalAggregate,
            baselineTagId,
            compareTagId
        )
    }

    @GetMapping("/data")
    fun getLogCompareData(authentication: Authentication,
                          @RequestParam startTime: String,
                          @RequestParam endTime: String,
                          @RequestParam(required = false) applicationId: Long?,
                          @RequestParam baselineTagId: String,
                          @RequestParam compareTagId: String): MutableList<LogCompareTable> {

        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "count_ad")
        return logCompareService.getLogCompareData(applicationsIndexes, startTime, endTime, user, baselineTagId, compareTagId)
    }


}