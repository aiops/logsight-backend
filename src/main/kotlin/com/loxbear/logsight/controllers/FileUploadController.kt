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
        val fileContent = file.inputStream.readBytes().toString(Charsets.UTF_8)
        return uploadFile(authentication, appID.toLong(), fileContent, LogFileTypes.valueOf(logFileType.toUpperCase()))
    }

    private fun uploadFile(
        authentication: Authentication,
        appID: Long,
        fileContent: String,
        type: LogFileTypes
    ): ResponseEntity<ApplicationResponse>{
        logService.processFileContent(authentication.name, appID, fileContent, type)

        return ResponseEntity(
            ApplicationResponse(
                type="",
                title="",
                instance="",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()), HttpStatus.OK
        )
    }
}