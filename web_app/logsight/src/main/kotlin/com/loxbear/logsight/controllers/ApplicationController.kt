package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.models.ApplicationRequest
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
        if (app != null) {
            return ResponseEntity(IdResponse(app.id), HttpStatus.OK)
        }
        else{
            return ResponseEntity("Please choose another name. The application already exists or incorrect name. The name of the application should contain only numbers and lowercase letters. Special signs are not allowed!", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/user/{key}")
    fun getApplicationsForUser(@PathVariable key: String): MutableList<com.loxbear.logsight.models.Application> {
        val user = usersService.findByKey(key)
        val applications = applicationService.findAllByUser(user)
        val returnApplications = mutableListOf<com.loxbear.logsight.models.Application>()
        for (i in applications.indices){
            returnApplications.add(com.loxbear.logsight.models.Application(id = applications[i].id, name = applications[i].name))
        }
        return returnApplications
    }

    @PostMapping("/{id}")
    fun deleteApplication(
        @PathVariable id: Long,
        @RequestParam(required = false) key: String?,
        authentication: Authentication?
    ): ResponseEntity<String> {
        if (authentication == null) {
            if (key == null || !usersService.existsByKey(key)) {
                return ResponseEntity("User is not authenticated or the user does not exist!", HttpStatus.BAD_REQUEST)
            }
        }
        return if (applicationService.deleteApplication(id)){
            ResponseEntity(HttpStatus.OK)
        }else{
            ResponseEntity("Application with the provided id does not exist!.", HttpStatus.BAD_REQUEST)
        }

    }
}