package com.loxbear.logsight.controllers


import com.loxbear.logsight.incidents.data.TopKIncidentsTable
import com.loxbear.logsight.services.elasticsearch.IncidentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/incidents")
class IncidentController(val incidentsService: IncidentService) {

    @GetMapping("/top_k_incidents")
    fun getTopKIncidentsTableData(): TopKIncidentsTable {
        return incidentsService.getTopKIncidentsTableData()
    }


}