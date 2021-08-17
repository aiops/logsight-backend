package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.PredefinedTime
import com.loxbear.logsight.models.*
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.PredefinedTimesService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.UsersService
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.postForEntity
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.lang.Thread.sleep


@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    val applicationService: ApplicationService,
    val usersService: UsersService,
    val kafkaService: KafkaService,
    val predefinedTimesService: PredefinedTimesService
) {
    val restTemplate = RestTemplateBuilder()
        .build();

    @Value("\${app.baseUrl}")
    private val appUrl: String? = null

    @PostMapping("/create")
    fun createApplication(@RequestBody body: ApplicationRequest): ResponseEntity<Any> {
        val user = usersService.findByKey(body.key)
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
    fun getApplicationsForUser(@PathVariable key: String): MutableList<Application> {
        val user = usersService.findByKey(key)
        val applications = applicationService.findAllByUser(user)
        val returnApplications = mutableListOf<com.loxbear.logsight.models.Application>()
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
            if (key == null || !usersService.existsByKey(key)) {
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
        val user = usersService.findByEmail(authentication.name)
        return predefinedTimesService.findAllByUser(user)
    }

    @PostMapping("/user/predefined_times")
    fun createPredefinedTimeForUser(
        authentication: Authentication,
        @RequestBody request: PredefinedTimeRequest
    ): PredefinedTime {
        val user = usersService.findByEmail(authentication.name)
        return predefinedTimesService.createPredefinedTimesForUser(user, request)
    }

    @DeleteMapping("/user/predefined_times/{id}")
    fun deletePredefinedTimeForUser(
        authentication: Authentication,
        @PathVariable id: Long
    ) {
        predefinedTimesService.deleteById(id)
    }


    @PostMapping("/uploadFile")
    fun uploadFile(authentication: Authentication, @RequestParam("file") file: MultipartFile, @RequestParam("info") info: String): ResponseEntity<ApplicationResponse> {
        val id = JSONObject(info).getLong("id")
        if (file.isEmpty) {
            return ResponseEntity(ApplicationResponse(
                description = "No file attached.",
                status = HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST)
        }
        try {
            val bytes = file.bytes
            val jsonLogs = fileBytesToJson(bytes)
            val jsonArrayLogs = jsonLogs.getJSONArray("log-messages")
            val user = usersService.findByEmail(authentication.name)
            val app = applicationService.findById(id)
            val processedLogs = processLogs(jsonArrayLogs, jsonLogs, app, user.key) // verify json, include timestamps, etc.
            var pageUrl = ""
            pageUrl = if (appUrl?.contains("logsight.ai") == true){
                appUrl.toString()
            } else {
                "http://localhost:5444"
            }
            this.restTemplate.postForEntity<String>(
                "$pageUrl/api_v1/data", processedLogs).body!!

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ResponseEntity(ApplicationResponse(
            description = "Data uploaded successfully.",
            status = HttpStatus.OK), HttpStatus.OK)
    }

    private fun processLogs(
        jsonArrayLogs: JSONArray,
        jsonLogs: JSONObject, application: com.loxbear.logsight.entities.Application?, userKey: String): Any {
        for (i in 0 until jsonArrayLogs.length()){
            jsonLogs.getJSONArray("log-messages").getJSONObject(i).put("private-key", userKey)
            if (application != null) {
                jsonLogs.getJSONArray("log-messages").getJSONObject(i).put("app", application.name)
            }
        }
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(jsonLogs.toString(), headers)
    }

    private fun fileBytesToJson(bytes: ByteArray): JSONObject {
        return JSONObject(String(bytes, Charsets.UTF_8))
    }


}