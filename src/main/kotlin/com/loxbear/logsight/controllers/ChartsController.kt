package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LogLevelPieChart
import com.loxbear.logsight.charts.data.LogLevelStackedLineChart
import com.loxbear.logsight.charts.data.SystemOverviewHeatmapChart
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UserService
import com.loxbear.logsight.services.elasticsearch.ChartsService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import utils.UtilsService


@RestController
@RequestMapping("/api/charts")
class ChartsController(val chartsService: ChartsService, val userService: UserService, val applicationService: ApplicationService) {

    @GetMapping("/log_level_advanced_pie_chart")
    fun getLogLevelPieData(authentication: Authentication,
                           @RequestParam startTime: String,
                           @RequestParam endTime: String): LogLevelPieChart {
        val user = userService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        return chartsService.getLogLevelPieChartData(applicationsIndexes, startTime, endTime, user.key)
    }

    @GetMapping("/dashboard_bar_anomalies")
    fun getAnomaliesBarChartData(authentication: Authentication,
                                 @RequestParam startTime: String,
                                 @RequestParam endTime: String): List<LineChart> {
        val user = userService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
//        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 4)
        return chartsService.getAnomaliesBarChartData(applicationsIndexes, startTime, endTime, user.key)
    }

    @GetMapping("/log_level_stacked_line_chart")
    fun getLogLevelStackedLineData(authentication: Authentication,
                                   @RequestParam startTime: String,
                                   @RequestParam endTime: String): LogLevelStackedLineChart {
        val user = userService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        return chartsService.getLogLevelStackedLineChartData(applicationsIndexes, startTime, endTime, user.key)
    }

    @GetMapping("/system_overview_heatmap")
    fun getSystemOverViewHeatmapData(authentication: Authentication,
                                     @RequestParam startTime: String,
                                     @RequestParam endTime: String,
                                     @RequestParam(required = false) compareTagId: String?,
                                     @RequestParam(required = false) baselineTagId: String?,
                                     @RequestParam(required = false) applicationId: Long?): SystemOverviewHeatmapChart {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user, application)
        return chartsService.getSystemOverviewHeatmapChart(
            applicationsIndexes,
            startTime,
            endTime,
            user,
            compareTagId,
            baselineTagId,
            null
        )
    }

    @GetMapping("/log_compare_heatmap")
    fun getLogCompareOverViewHeatmapData(authentication: Authentication,
                                     @RequestParam startTime: String,
                                     @RequestParam endTime: String,
                                     @RequestParam compareTagId: String?,
                                     @RequestParam baselineTagId: String?,
                                     @RequestParam applicationId: Long?): SystemOverviewHeatmapChart {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        var applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user, application)
        if (compareTagId != null && baselineTagId != null){
            applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "count_ad")
        }
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        return chartsService.getSystemOverviewHeatmapChart(applicationsIndexes, startTime, endTime, user, compareTagId, baselineTagId, intervalAggregate)
    }

    @GetMapping("/log_comp_new_templates_bar")
    fun getNewTemplatesBarChartData(authentication: Authentication,
                                 @RequestParam startTime: String,
                                 @RequestParam endTime: String,
                                @RequestParam applicationId: Long?,
                                    @RequestParam compareTagId: String?,
                                    @RequestParam baselineTagId: String?): MutableList<LineChart> {
        val user = userService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForLogCompare(user, application, "count_ad")
        val intervalAggregate = UtilsService.getTimeIntervalAggregate(startTime, endTime, 10)
        return chartsService.getNewTemplatesBarChartData(applicationsIndexes, startTime, endTime, user.key,
            baselineTagId, compareTagId, intervalAggregate)
    }
}