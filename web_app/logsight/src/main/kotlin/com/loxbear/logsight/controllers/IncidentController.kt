package com.loxbear.logsight.controllers

import com.loxbear.logsight.incidents.data.TopKIncidentTable
import com.loxbear.logsight.services.UsersService
import com.loxbear.logsight.services.elasticsearch.IncidentService
import org.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/incidents")
class IncidentController(val incidentsService: IncidentService, val usersService: UsersService) {

    @GetMapping("/top_k_incidents")
    fun getTopKIncidentsTableData(authentication: Authentication): List<TopKIncidentTable> {
        val user = usersService.findByEmail(authentication.name)
        val esIndexUserApp = "${user.key}_grozdan212_incidents"
//        val esIndexUserApp = "1234-213_app_name_test_incidents"
        val startTime = "now-12h"
        val stopTime = "now"
        return incidentsService.getTopKIncidentsTableData(esIndexUserApp, startTime, stopTime)
    }


}