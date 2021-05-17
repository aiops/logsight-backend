package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.IncidentTableData
import com.loxbear.logsight.charts.data.LineChartSeries
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
    fun getTopKIncidentsTableData(authentication: Authentication,
                                  @RequestParam startTime: String,
                                  @RequestParam endTime: String,
                                  @RequestParam(required = false) applicationId: Long?): List<TopKIncidentTable> {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user, application)
        return incidentsService.getTopKIncidentsTableData(applicationsIndexes, startTime, endTime, user)
    }

    @GetMapping("/bar_chart_data")
    fun getIncidentsBarChartData(authentication: Authentication,
                                 @RequestParam startTime: String,
                                 @RequestParam endTime: String,
                                 @RequestParam(required = false) applicationId: Long?): List<LineChartSeries> {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user, application)
        return incidentsService.getIncidentsBarChartData(applicationsIndexes, startTime, endTime)
    }

    @GetMapping("/table_data")
    fun getIncidentsTableData(authentication: Authentication,
                              @RequestParam startTime: String,
                              @RequestParam endTime: String,
                              @RequestParam(required = false) applicationId: Long?): IncidentTableData {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationId?.let { applicationService.findById(applicationId) }
        val applicationsIndexes = applicationService.getApplicationIndexesForIncidents(user, application)
        return incidentsService.getIncidentsTableData(applicationsIndexes, startTime, endTime)
    }
}