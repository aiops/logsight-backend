package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.enums.LogFileType
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.LogService
import com.loxbear.logsight.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/uploadFile")
class LogFileController(
    val logService: LogService,
    val applicationService: ApplicationService,
    val userService: UserService
) {

    @PostMapping("/json_file")
    fun uploadFileJson(
        authentication: Authentication,
        @RequestParam("appID") appID: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ApplicationResponse> {
        return uploadFile(authentication, appID, file, LogFileType.LOSIGHT_JSON)
    }

    @PostMapping("/syslog_file")
    fun uploadFileSyslog(
        authentication: Authentication,
        @RequestParam("appID") appID: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ApplicationResponse> {
        return uploadFile(authentication, appID, file, LogFileType.SYSLOG)
    }

    private fun uploadFile(
        authentication: Authentication,
        appID: String,
        file: MultipartFile,
        type: LogFileType
    ): ResponseEntity<ApplicationResponse>{

        logService.processFile(authentication.name, appID, file, type)

        return ResponseEntity(
            ApplicationResponse(
                description = "Data uploaded successfully.",
                status = HttpStatus.OK), HttpStatus.OK)
    }
}