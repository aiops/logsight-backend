package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.IncidentTableData
import com.loxbear.logsight.charts.data.IncidentTimelineData
import com.loxbear.logsight.charts.data.TopKIncidentTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import com.loxbear.logsight.services.elasticsearch.IncidentService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/incidents")
class IncidentsController(val incidentsService: IncidentService, val usersService: UsersService,
                          val applicationService: ApplicationService) {

    @GetMapping("/top_k_incidents")
    fun getTopKIncidentsTableData(authentication: Authentication): List<TopKIncidentTable> {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user)
        val startTime = "now-12h"
        val stopTime = "now"
        return incidentsService.getTopKIncidentsTableData(applicationsIndexes, startTime, stopTime)
    }

    @GetMapping("/bar_chart_data")
    fun getIncidentsTimelineData(authentication: Authentication,
                                 @RequestParam startTime: String,
                                 @RequestParam endTime: String): List<IncidentTimelineData> {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user)
        return incidentsService.getIncidentsBarChartData(applicationsIndexes, startTime, endTime)
    }

    @GetMapping("/table_data")
    fun getIncidentsTableData(authentication: Authentication,
                              @RequestParam startTime: String,
                              @RequestParam endTime: String): IncidentTableData {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user)
        return incidentsService.getIncidentsTableData(applicationsIndexes, startTime, endTime)
    }
}