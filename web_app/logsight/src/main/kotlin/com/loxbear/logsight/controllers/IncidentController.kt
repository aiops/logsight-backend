package com.loxbear.logsight.controllers

import com.loxbear.logsight.incidents.data.TopKIncidentTable
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import com.loxbear.logsight.services.elasticsearch.IncidentService
import org.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/incidents")
class IncidentController(val incidentsService: IncidentService, val usersService: UsersService,
                         val applicationService: ApplicationService) {

    @GetMapping("/top_k_incidents")
    fun getTopKIncidentsTableData(authentication: Authentication): List<TopKIncidentTable> {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        val startTime = "now-12h"
        val stopTime = "now"
        return incidentsService.getTopKIncidentsTableData(applicationsIndexes, startTime, stopTime)
    }

    @GetMapping("/bar_chart_data")
    fun getIncidentsBarChartData(authentication: Authentication, @RequestParam startTime: String, endTime: String) {
        val user = usersService.findByEmail(authentication.name)
        val applicationsIndexes = applicationService.getApplicationIndexes(user)
        val startTimee = "now-12h"
        val stopTime = "now"
        incidentsService.getIncidentsBarChartData(applicationsIndexes, startTimee, stopTime)
    }
}