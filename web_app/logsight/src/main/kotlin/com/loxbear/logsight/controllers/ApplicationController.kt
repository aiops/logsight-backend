package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.models.ApplicationRequest
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    val applicationService: ApplicationService,
    val usersService: UsersService
) {

    @PostMapping
    fun createApplication(@RequestBody body: ApplicationRequest): Application {
        val user = usersService.findByKey(body.key)
        return applicationService.createApplication(body.name, user)
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
    ): ResponseEntity.BodyBuilder {
        if (authentication == null) {
            if (key == null || !usersService.existsByKey(key)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
            }
        }
        applicationService.deleteApplication(id)
        return ResponseEntity.status(HttpStatus.OK)
    }
}