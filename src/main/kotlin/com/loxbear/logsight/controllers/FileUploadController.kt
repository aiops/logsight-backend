package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.enums.LogFileTypes
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
@RequestMapping("/api/applications/{appID}/uploadFile")
class FileUploadController(
    val logService: LogService,
    val applicationService: ApplicationService,
    val userService: UserService
) {

    val log: Logger = Logger.getLogger(FileUploadController::class.java.toString())

    @GetMapping("/")
    fun uploaderStatus(@PathVariable appID: Long): ResponseEntity<String> {
        return ResponseEntity(
            "Ready to receive log data files for app $appID.",
            HttpStatus.OK
        )
    }

    @PostMapping("/{logFileType}")
    fun uploadFileJson(
        authentication: Authentication,
        @PathVariable appID: String,
        @PathVariable logFileType: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ApplicationResponse> {
        return uploadFile(authentication, appID.toLong(), file, LogFileTypes.valueOf(logFileType.toUpperCase()))
    }

    private fun uploadFile(
        authentication: Authentication,
        appID: Long,
        file: MultipartFile,
        type: LogFileTypes
    ): ResponseEntity<ApplicationResponse>{
        logService.processFile(authentication.name, appID, file, type)

        return ResponseEntity(
            ApplicationResponse(
                description = "Data uploaded successfully.",
                status = HttpStatus.OK), HttpStatus.OK
        )
    }
}