package com.loxbear.logsight.controllers

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.charts.elasticsearch.VariableAnalysisHit
import com.loxbear.logsight.models.SpecificTemplateRequest
import com.loxbear.logsight.models.TopNTemplatesData
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
        val startTime = "now-15m"
        val stopTime = "now"
        return variableAnalysisService.getTemplates(applicationsIndexes, startTime, stopTime, search)
    }

    @PostMapping("/application/{id}/specific_template")
    fun getSpecificTemplate(@PathVariable id: Long,
                            @RequestBody specificTemplate: SpecificTemplateRequest,
                            authentication: Authentication): Pair<String, List<LineChart>> {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationService.findById(id)
        val applicationsIndexes = variableAnalysisService.getApplicationIndex(application, user.key)
        val startTime = "now-2h"
        val stopTime = "now"
        with(specificTemplate) {
            val tmp = variableAnalysisService.getSpecificTemplateGrouped(applicationsIndexes, startTime, stopTime, template, param, paramValue)
            return variableAnalysisService.getSpecificTemplateGrouped(applicationsIndexes, startTime, stopTime, template, param, paramValue)
        }
    }

    @GetMapping("/application/{id}/top_n_templates")
    fun getTop5Templates(@PathVariable id: Long, authentication: Authentication): Map<String, List<TopNTemplatesData>> {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationService.findById(id)
        val applicationsIndexes = variableAnalysisService.getApplicationIndex(application, user.key)
        return variableAnalysisService.getTopNTemplates(applicationsIndexes)
    }

    @GetMapping("/application/{id}/log_count_line_chart")
    fun getLogCountLineChart(@PathVariable id: Long, authentication: Authentication): List<LineChart> {
        val user = usersService.findByEmail(authentication.name)
        val application = applicationService.findById(id)
        val applicationsIndexes = variableAnalysisService.getApplicationIndex(application, user.key)
        val startTime = "now-15m"
        val stopTime = "now"
        return variableAnalysisService.getLogCountLineChart(applicationsIndexes, startTime, stopTime)
    }

}