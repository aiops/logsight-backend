package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import com.loxbear.logsight.services.elasticsearch.VariableAnalysisService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/variable-analysis")
class VariableAnalysisController(val variableAnalysisService: VariableAnalysisService,
                                 val usersService: UsersService, val applicationService: ApplicationService) {

    @GetMapping("/application/{id}")
    fun getTemplates(@PathVariable id: Long, @RequestParam(required = false) search: String?, authentication: Authentication): List<VariableAnalysisHit> {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationService.findById(id)
        val applicationsIndexes = variableAnalysisService.getApplicationIndex(application, user.key)
        val startTime = "now-1h"
        val stopTime = "now"
        return variableAnalysisService.getTemplates(applicationsIndexes, startTime, stopTime)
    }
}