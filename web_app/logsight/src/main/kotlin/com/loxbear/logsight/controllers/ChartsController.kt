package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LogLevelPieChart
import com.loxbear.logsight.charts.data.LogLevelStackedLineChart
import com.loxbear.logsight.charts.data.SystemOverviewHeatmapChart
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import com.loxbear.logsight.services.elasticsearch.ChartsService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/charts")
class ChartsController(val chartsService: ChartsService, val usersService: UsersService, val applicationService: ApplicationService) {

    @GetMapping("/log_level_advanced_pie_chart")
    fun getLogLevelPieData(authentication: Authentication,
                           @RequestParam startTime: String,
                           @RequestParam endTime: String): LogLevelPieChart {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        return chartsService.getLogLevelPieChartData(applicationsIndexes, startTime, endTime)
    }

    @GetMapping("/dashboard_bar_anomalies")
    fun getAnomaliesBarChartData(authentication: Authentication,
                                 @RequestParam startTime: String,
                                 @RequestParam endTime: String): List<LineChart> {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        return chartsService.getAnomaliesBarChartData(applicationsIndexes, startTime, endTime)
    }

    @GetMapping("/log_level_stacked_line_chart")
    fun getLogLevelStackedLineData(authentication: Authentication,
                                   @RequestParam startTime: String,
                                   @RequestParam endTime: String): LogLevelStackedLineChart {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        return chartsService.getLogLevelStackedLineChartData(applicationsIndexes, startTime, endTime)
    }

    @GetMapping("/system_overview_heatmap")
    fun getSystemOverViewHeatmapData(authentication: Authentication,
                                     @RequestParam startTime: String,
                                     @RequestParam endTime: String): SystemOverviewHeatmapChart {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user)
        return chartsService.getSystemOverviewHeatmapChart(applicationsIndexes, startTime, endTime)
    }
}