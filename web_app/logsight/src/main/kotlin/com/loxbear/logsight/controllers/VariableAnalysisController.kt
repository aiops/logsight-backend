package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.models.ApplicationRequest
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/variable-analysis")
class VariableAnalysisController(val applicationService: ApplicationService,
                                 val usersService: UsersService) {

    @PostMapping
    fun createApplication(@RequestBody body: ApplicationRequest): Application {
        val user = usersService.findByKey(body.key)
        return applicationService.createApplication(body.name, user)
    }

    @GetMapping("/application/{id}")
    fun getApplicationsForUser(@PathVariable id: Long, @RequestParam(required = false) search: String?) {

    }
}