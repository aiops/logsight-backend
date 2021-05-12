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
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/charts")
class ChartsController(val chartsService: ChartsService, val usersService: UsersService, val applicationService: ApplicationService) {


//    @GetMapping("/line_chart")
//    fun getLineChartData(): LineChart {
//        return chartsService.getLineChartData()
//    }

    @GetMapping("/log_level_advanced_pie_chart")
    fun getLogLevelPieData(authentication: Authentication): LogLevelPieChart {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        val startTime = "now-12h"
        val stopTime = "now"
        return chartsService.getLogLevelPieChartData(applicationsIndexes, startTime, stopTime)
    }

    @GetMapping("/dashboard_bar_anomalies")
    fun getAnomaliesBarChartData(authentication: Authentication): List<LineChart> {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        val startTime = "now-12h"
        val stopTime = "now"
        val tmp = chartsService.getAnomaliesBarChartData(applicationsIndexes, startTime, stopTime)
        return tmp
    }

    @GetMapping("/log_level_stacked_line_chart")
    fun getLogLevelStackedLineData(authentication: Authentication): LogLevelStackedLineChart {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        val startTime = "now-12h"
        val stopTime = "now"
        return chartsService.getLogLevelStackedLineChartData(applicationsIndexes, startTime, stopTime)
    }


    @GetMapping("/system_overview_heatmap")
    fun getSystemOverViewHeatmapData(authentication: Authentication): SystemOverviewHeatmapChart {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user)
        val startTime = "now-12h"
        val stopTime = "now"
        return chartsService.getSystemOverviewHeatmapChart(applicationsIndexes, startTime, stopTime)
    }


}