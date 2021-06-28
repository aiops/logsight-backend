package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.models.ApplicationRequest
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UsersService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.postForEntity
import utils.UtilsService.Companion.createKibanaRequestWithHeaders


@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    val applicationService: ApplicationService,
    val usersService: UsersService
) {
    val restTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build();

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @PostMapping
    fun createApplication(@RequestBody body: ApplicationRequest): Application {
        val user = usersService.findByKey(body.key)
        return applicationService.createApplication(body.name, user)
    }


    @PostMapping("/kibana/login")
    fun kibanaLogin(@RequestBody requestBody: String): ResponseEntity<String> {
        val request = createKibanaRequestWithHeaders(requestBody)
        val response = restTemplate.postForEntity<String>("http://localhost:5601/kibana/api/security/v1/login", request)
        return response
    }

    @GetMapping("/user/{key}")
    fun getApplicationsForUser(@PathVariable key: String): List<Application> {
        val user = usersService.findByKey(key)
        return applicationService.findAllByUser(user)
    }

    @PostMapping("/{id}")
    fun deleteApplication(@PathVariable id: Long) {
        applicationService.deleteApplication(id)
    }
}