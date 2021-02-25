package com.loxbear.logsight.controllers

import com.loxbear.logsight.incidents.data.TopKIncidentTable
import com.loxbear.logsight.services.elasticsearch.IncidentService
import org.json.JSONObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/incidents")
class IncidentController(val incidentsService: IncidentService) {

    @GetMapping("/top_k_incidents")
    fun getTopKIncidentsTableData(): List<TopKIncidentTable> {
        val esIndexUserApp = "1234-213_app_name_test_incidents"
        val startTime = "now-12h"
        val stopTime = "now"
        return incidentsService.getTopKIncidentsTableData(esIndexUserApp, startTime, stopTime)
    }


}