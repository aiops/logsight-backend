package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.Application
import com.loxbear.logsight.models.ApplicationRequest
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.models.IdResponse
import com.loxbear.logsight.models.log.LogFileType
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    val applicationService: ApplicationService,
    val userService: UserService,
    @Value("\${user.appLimit}") private val userAppLimit: Int
) {

    @GetMapping("")
    fun getApplications(
        authentication: Authentication,
    ): ResponseEntity<Any> {
        val user = userService.findByEmail(authentication.name)
        if (user.isEmpty)
            return ResponseEntity(null, HttpStatus.UNAUTHORIZED)
        return ResponseEntity(applicationService.findAllByUser(user.get()), HttpStatus.OK)
    }

    @GetMapping("/{appName}")
    fun getApplication(
        authentication: Authentication,
        @PathVariable appName: String
    ): ResponseEntity<Any> {
        val user = userService.findByEmail(authentication.name)
        if (user.isEmpty)
            return ResponseEntity(null, HttpStatus.UNAUTHORIZED)
        val app = applicationService.findByUserAndName(user.get(), appName)
        if (app.isEmpty)
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        return ResponseEntity(app.get(), HttpStatus.OK)
    }

    @PostMapping("/create")
    suspend fun createApplication(
        @RequestBody body: ApplicationRequest,
    ): ResponseEntity<Any> {
        val user = try {
            userService.findByKey(body.key)
        } catch (e: Exception) {
            return ResponseEntity(
                ApplicationResponse(
                    type = "Error",
                    title = "private-key is not valid",
                    status = HttpStatus.UNAUTHORIZED.value(),
                    detail = "Please check your private key. Seems that is invalid.",
                    instance = "api/applications/create"
                ),
                HttpStatus.UNAUTHORIZED
            )
        }

        if (applicationService.findAllByUser(user).size >= userAppLimit) {
            return ResponseEntity(
                ApplicationResponse(
                    type = "Error",
                    title = "Application limit is reached, please contact support!",
                    status = HttpStatus.BAD_REQUEST.value(),
                    detail = "Maximum 5 applications are allowed",
                    instance = "api/applications/create"
                ),
                HttpStatus.BAD_REQUEST
            )
        }
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
                    type = "Error",
                    title = "Application name already exists or invalid.",
                    status = HttpStatus.BAD_REQUEST.value(),
                    detail = "Please choose another name. The application already exists or incorrect name. " +
                            "The name of the application should contain only numbers and lowercase letters. " +
                            "Special signs are not allowed(except underscore)!",
                    instance = "api/applications/create"
                ), HttpStatus.BAD_REQUEST
            )
        }
    }

    @PostMapping("/create/demo_apps")
    fun createDemoApplications(
        authentication: Authentication
    ): ResponseEntity<Any> = userService.findByEmail(authentication.name).let { userOpt ->
        userOpt.ifPresent { user ->
            suspend {
                applicationService.createApplication("compute_sample_app", user)
                applicationService.createApplication("auth_sample_app", user)
                applicationService.createApplication("auth2_sample_app", user)
            }
        }
        if (userOpt.isPresent)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.BAD_REQUEST)
    }


    @GetMapping("/user/{key}")
    fun getApplicationsForUser(@PathVariable key: String): Any {
        try {
            val user = userService.findByKey(key)
        } catch (e: Exception) {
            return ResponseEntity(
                ApplicationResponse(
                    type = "Error",
                    title = "private-key is not valid",
                    status = HttpStatus.UNAUTHORIZED.value(),
                    detail = "Please check your private key. Seems that is invalid.",
                    instance = "api/applications/create"
                ),
                HttpStatus.UNAUTHORIZED
            )
        }
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
                        type = "Error",
                        title = "User does not exist or not authenticated",
                        status = HttpStatus.BAD_REQUEST.value(),
                        detail = "User is not authenticated or the user does not exist!",
                        instance = "api/applications/delete"
                    ), HttpStatus.BAD_REQUEST
                )
            }
        }
        applicationService.deleteApplication(id)
        return ResponseEntity(
            ApplicationResponse(
                type = "",
                title = "",
                status = HttpStatus.OK.value(),
                detail = "Application deleted successfully.",
                instance = "api/applications/delete"
            ), HttpStatus.OK
        )
    }

    @GetMapping("/logFileFormats")
    fun getLogFileFormats(): Collection<LogFileType> {
        return LogFileTypes.values().map { LogFileType(it.toString().toLowerCase(), it.frontEndDescriptor) }
    }

    @GetMapping("/update_kibana_patterns")
    fun updateKibanaPatterns(authentication: Authentication) {
        userService.findByEmail(authentication.name).map { user ->
            applicationService.updateKibanaPatterns(user)
        }
    }

}