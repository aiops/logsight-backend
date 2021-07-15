package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.models.ApplicationRequest
import com.loxbear.logsight.models.IdResponse
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    val applicationService: ApplicationService,
    val usersService: UsersService
) {

    @PostMapping("/create")
    fun createApplication(@RequestBody body: ApplicationRequest): IdResponse {
        val user = usersService.findByKey(body.key)
        val app = applicationService.createApplication(body.name, user)
        return IdResponse(app.id)
    }

    @GetMapping("/user/{key}")
    fun getApplicationsForUser(@PathVariable key: String): List<Application> {
        val user = usersService.findByKey(key)
        return applicationService.findAllByUser(user)
    }

    @PostMapping("/{id}")
    fun deleteApplication(
        @PathVariable id: Long,
        @RequestParam(required = false) key: String?,
        authentication: Authentication?
    ): HttpStatus {
        if (authentication == null) {
            if (key == null || !usersService.existsByKey(key)) {
                return HttpStatus.FORBIDDEN
            }
        }
        applicationService.deleteApplication(id)
        return HttpStatus.OK
    }
}