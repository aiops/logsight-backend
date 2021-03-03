package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.data.LogLevelPieChart
import com.loxbear.logsight.charts.data.LogLevelStackedLineChart
import com.loxbear.logsight.charts.data.SystemOverviewHeatmapChart
import com.loxbear.logsight.services.UsersService
import com.loxbear.logsight.services.elasticsearch.ChartsService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/charts")
class ChartsController(val chartsService: ChartsService, val usersService: UsersService) {


    @GetMapping("/line_chart")
    fun getLineChartData(): LineChart {
        return chartsService.getLineChartData()
    }

    @GetMapping("/log_level_advanced_pie_chart")
    fun getLogLevelPieData(authentication: Authentication): LogLevelPieChart {
        val user = usersService.findByEmail(authentication.name)
        print(user)
        val esIndexUserApp = "${user.key.toLowerCase().filter { it.isLetterOrDigit() }}_grozdan212222_log_ad"//"1234-213_app_name_test_log_ad" // can be multiple indices (multiple apps)
        // in that case, we just append them with comma $esIndexUserApp1,$esIndexUserApp2 ...
        val startTime = "now-1h"
        val stopTime = "now"
        return chartsService.getLogLevelPieChartData(esIndexUserApp, startTime, stopTime)
    }

    @GetMapping("/log_level_stacked_line_chart")
    fun getLogLevelStackedLineData(authentication: Authentication): LogLevelStackedLineChart {
        val user = usersService.findByEmail(authentication.name)
//        val esIndexUserApp = "1234-213_app_name_test_log_ad" // can be multiple indices (multiple apps)
        val esIndexUserApp = "${user.key.toLowerCase().filter { it.isLetterOrDigit() }}_grozdan212222_log_ad"
        // in that case, we just append them with comma $esIndexUserApp1,$esIndexUserApp2 ...
        val startTime = "now-1h"
        val stopTime = "now"
        return chartsService.getLogLevelStackedLineChartData(esIndexUserApp, startTime, stopTime)
    }


    @GetMapping("/system_overview_heatmap")
    fun getSystemOverViewHeatmapData(authentication: Authentication): SystemOverviewHeatmapChart {
        val user = usersService.findByEmail(authentication.name)
//        val esIndexUserAppLogAd = "1234-213_app_name_test_log_ad" // this should be list of all indices
        // (count_ad and log_ad) of the apps belonging to the user
        val esIndexUserApp = "${user.key.toLowerCase().filter { it.isLetterOrDigit() }}_grozdan212222_log_ad"
        val startTime = "now-2h"
        val stopTime = "now"
        return chartsService.getSystemOverviewHeatmapChart(esIndexUserApp, startTime, stopTime)
    }


}