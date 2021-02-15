package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.services.elasticsearch.ChartsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/charts")
class ChartsController(val chartsService: ChartsService) {


    @GetMapping("/line_chart")
    fun getLineChartData(): LineChart {
        val data = chartsService.getLineChartData()
        println(data)
        return data
    }

}