package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.IncidentTableData
import com.loxbear.logsight.charts.data.LineChartSeries
import com.loxbear.logsight.charts.data.TopKIncidentTable
import com.loxbear.logsight.charts.elasticsearch.LogQualityOverview
import com.loxbear.logsight.models.LogQualityTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UserService
import com.loxbear.logsight.services.elasticsearch.IncidentService
import com.loxbear.logsight.services.elasticsearch.LogCompareService
import com.loxbear.logsight.services.elasticsearch.QualityService
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import utils.UtilsService.Companion.getTimeIntervalAggregate

@RestController
@RequestMapping("/api/quality")
class QualityController(val qualityService: QualityService, val userService: UserService,
                          val applicationService: ApplicationService) {

    val restTemplate = RestTemplateBuilder()
        .build();


    @GetMapping("/overall_quality")
    fun getLogQualityData(authentication: Authentication,
                                  @RequestParam startTime: String,
                                  @RequestParam endTime: String,
                                  @RequestParam(required = false) applicationId: Long?): MutableList<LogQualityTable> {

        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForQuality(user, application)
        val qualityData = qualityService.getLogQualityData(applicationsIndexes, startTime, endTime, user)
        return qualityData
    }

    @GetMapping("/overall_quality_overview")
    fun getLogQualityOverview(authentication: Authentication,
                          @RequestParam startTime: String,
                          @RequestParam endTime: String,
                          @RequestParam(required = false) applicationId: Long?): MutableList<LogQualityOverview> {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForQuality(user, application)
        val qualityData = qualityService.getLogQualityOverview(applicationsIndexes, startTime, endTime, user)
        return qualityData
    }


    @GetMapping("/compute_log_quality")
    fun computeLogQuality(authentication: Authentication,
                              @RequestParam startTime: String,
                              @RequestParam endTime: String,
                              @RequestParam(required = false) applicationId: Long?): HttpStatus {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForQuality(user, application)
        return qualityService.computeLogQuality(applicationsIndexes, startTime, endTime, user)
    }

}