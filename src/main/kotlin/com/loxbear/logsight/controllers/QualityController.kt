package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.elasticsearch.LogQualityOverview
import com.loxbear.logsight.models.LogQualityTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UserService
import com.loxbear.logsight.services.elasticsearch.QualityService
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/quality")
class QualityController(
    val qualityService: QualityService, val userService: UserService,
    val applicationService: ApplicationService
) {

    val restTemplate = RestTemplateBuilder()
        .build()


    @GetMapping("/overall_quality")
    fun getLogQualityData(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long?
    ): MutableList<LogQualityTable> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForQuality(user, application?.orElse(null))
        val qualityData = qualityService.getLogQualityData(applicationsIndexes, startTime, endTime, user)
        qualityData
    }.orElse(mutableListOf())

    @GetMapping("/overall_quality_overview")
    fun getLogQualityOverview(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long?
    ): MutableList<LogQualityOverview> = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForQuality(user, application?.orElse(null))
        val qualityData = qualityService.getLogQualityOverview(applicationsIndexes, startTime, endTime, user)
        qualityData
    }.orElse(mutableListOf())


    @GetMapping("/compute_log_quality")
    fun computeLogQuality(
        authentication: Authentication,
        @RequestParam startTime: String,
        @RequestParam endTime: String,
        @RequestParam(required = false) applicationId: Long?
    ): HttpStatus = userService.findByEmail(authentication.name).map { user ->
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForQuality(user, application?.orElse(null))
        qualityService.computeLogQuality(applicationsIndexes, startTime, endTime, user)
    }.orElse(HttpStatus.BAD_REQUEST)

}