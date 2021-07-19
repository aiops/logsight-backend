package com.loxbear.logsight.controllers

import com.loxbear.logsight.models.Application
import com.loxbear.logsight.models.ApplicationRequest
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.models.IdResponse
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

    @PostMapping("/create")
    fun createApplication(@RequestBody body: ApplicationRequest): ResponseEntity<Any> {
        val user = usersService.findByKey(body.key)
        val app = applicationService.createApplication(body.name, user)
        return if (app != null) {
            ResponseEntity(IdResponse(description = "Application created successfully!", status = HttpStatus.OK,
                id = app.id),
                HttpStatus.OK)
        } else{
            ResponseEntity(ApplicationResponse(
                description= "Please choose another name. The application already exists or incorrect name. " +
                        "The name of the application should contain only numbers and lowercase letters. " +
                        "Special signs are not allowed(except underscore)!", status = HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/user/{key}")
    fun getApplicationsForUser(@PathVariable key: String): MutableList<com.loxbear.logsight.models.Application> {
        val user = usersService.findByKey(key)
        val applications = applicationService.findAllByUser(user)
        val returnApplications = mutableListOf<com.loxbear.logsight.models.Application>()
        for (i in applications.indices){
            returnApplications.add(com.loxbear.logsight.models.Application(description = "Application list",
                status = HttpStatus.OK,
                id = applications[i].id,
                name = applications[i].name))
        }
        return returnApplications.sortedBy { it.name } as MutableList<Application>
    }

    @PostMapping("/{id}")
    fun deleteApplication(
        @PathVariable id: Long,
        @RequestParam(required = false) key: String?,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        if (authentication == null) {
            if (key == null || !usersService.existsByKey(key)) {
                return ResponseEntity(ApplicationResponse(
                    description = "User is not authenticated or the user does not exist!",
                    status = HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST)
            }
        }
        return if (applicationService.deleteApplication(id)){
            ResponseEntity(ApplicationResponse(
                description = "Application deleted successfully.",
                status = HttpStatus.OK), HttpStatus.OK)
        }else{
            ResponseEntity(ApplicationResponse(
                description = "Application with the provided id does not exist!",
                status = HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST)
        }

    }
}