package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.enums.LogFileType
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.LogService
import com.loxbear.logsight.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.logging.Logger

@RestController
@RequestMapping("/api/application/{appID}/uploadFile")
class LogFileController(
    val logService: LogService,
    val applicationService: ApplicationService,
    val userService: UserService
) {

    val log: Logger = Logger.getLogger(LogFileController::class.java.toString())

    @GetMapping("/")
    fun uploaderStatus(@PathVariable appID: Long): ResponseEntity<ApplicationResponse> {
        return ResponseEntity(
            ApplicationResponse(
                description = "Ready to receive log data files for app $appID.",
                status = HttpStatus.OK),
            HttpStatus.OK)
    }

    @PostMapping("/logsightjson")
    fun uploadFileJson(
        authentication: Authentication,
        @PathVariable appID: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ApplicationResponse> {
        return uploadFile(authentication, appID.toLong(), file, LogFileType.LOSIGHT_JSON)
    }

    @PostMapping("/syslog")
    fun uploadFileSyslog(
        authentication: Authentication,
        @PathVariable appID: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ApplicationResponse> {
        return uploadFile(authentication, appID, file, LogFileType.SYSLOG)
    }

    private fun uploadFile(
        authentication: Authentication,
        appID: Long,
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