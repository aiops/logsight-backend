package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.PredefinedTime
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.*
import com.loxbear.logsight.models.log.LogFileType
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.PredefinedTimesService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    val applicationService: ApplicationService,
    val userService: UserService,
    val predefinedTimesService: PredefinedTimesService
) {
    val restTemplate = RestTemplateBuilder()
        .build();

    @Value("\${app.baseUrl}")
    private val appUrl: String? = null

    @PostMapping("/create")
    fun createApplication(@RequestBody body: ApplicationRequest): ResponseEntity<Any> {
        val user = userService.findByKey(body.key)
        val app = applicationService.createApplication(body.name, user)
        return if (app != null) {
            ResponseEntity(
                IdResponse(
                    description = "Application created successfully!", status = HttpStatus.OK,
                    id = app.id
                ),
                HttpStatus.OK
            )
        } else {
            ResponseEntity(
                ApplicationResponse(
                    description = "Please choose another name. The application already exists or incorrect name. " +
                        "The name of the application should contain only numbers and lowercase letters. " +
                        "Special signs are not allowed(except underscore)!", status = HttpStatus.BAD_REQUEST
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("/user/{key}")
    fun getApplicationsForUser(@PathVariable key: String): Collection<Application> {
        val user = userService.findByKey(key)
        val applications = applicationService.findAllByUser(user)
        val returnApplications = mutableListOf<Application>()
        for (i in applications.indices) {
            returnApplications.add(
                Application(
                    description = "Application list",
                    status = HttpStatus.OK,
                    id = applications[i].id,
                    name = applications[i].name
                )
            )
        }
        return if (returnApplications.size > 0) {
            returnApplications.sortedBy { it.name } as MutableList<Application>
        } else {
            returnApplications
        }
    }

    @PostMapping("/{id}")
    fun deleteApplication(
        @PathVariable id: Long,
        @RequestParam(required = false) key: String?,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        if (authentication == null) {
            if (key == null || !userService.existsByKey(key)) {
                return ResponseEntity(
                    ApplicationResponse(
                        description = "User is not authenticated or the user does not exist!",
                        status = HttpStatus.BAD_REQUEST
                    ), HttpStatus.BAD_REQUEST
                )
            }
        }
        return if (applicationService.deleteApplication(id)) {
            ResponseEntity(
                ApplicationResponse(
                    description = "Application deleted successfully.",
                    status = HttpStatus.OK
                ), HttpStatus.OK
            )
        } else {
            ResponseEntity(
                ApplicationResponse(
                    description = "Application with the provided id does not exist!",
                    status = HttpStatus.BAD_REQUEST
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("/user/predefined_times")
    fun getPredefinedTimesForUser(authentication: Authentication): List<PredefinedTime> {
        val user = userService.findByEmail(authentication.name)
        return predefinedTimesService.findAllByUser(user)
    }

    @PostMapping("/user/predefined_times")
    fun createPredefinedTimeForUser(
        authentication: Authentication,
        @RequestBody request: PredefinedTimeRequest
    ): PredefinedTime {
        val user = userService.findByEmail(authentication.name)
        return predefinedTimesService.createPredefinedTimesForUser(user, request)
    }

    @DeleteMapping("/user/predefined_times/{id}")
    fun deletePredefinedTimeForUser(
        authentication: Authentication,
        @PathVariable id: Long
    ) {
        predefinedTimesService.deleteById(id)
    }

    @GetMapping("/logFileFormats")
    fun getLogFileFormats(authentication: Authentication): Collection<LogFileType> {
        return LogFileTypes.values().map{ LogFileType(it.toString().toLowerCase(), it.frontEndDescriptor) }
    }
}