package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.LogService
import com.loxbear.logsight.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.logging.Logger

@RestController
@RequestMapping("/api/logs")
class LogMessageController(
    val logService: LogService,
    val applicationService: ApplicationService,
    val userService: UserService,
    val kafkaService: KafkaService
) {

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    val logger: org.slf4j.Logger = LoggerFactory.getLogger(ApplicationService::class.java)

    @PostMapping("/{userKey}/{applicationName}/send_logs")
    fun sendLogs(
        authentication: Authentication,
        @RequestBody logs: List<String>,
        @PathVariable userKey: String,
        @PathVariable applicationName: String
    ): ResponseEntity<ApplicationResponse> {
        val user = userService.findByKey(userKey)
        val application = applicationService.findByUserAndName(user, applicationName).get()
        return sendLogsForProcessing(user.key, user.email, application.name, application.inputTopicName, application.id, LogFileTypes.UNKNOWN_FORMAT, logs)
    }

    private fun sendLogsForProcessing(
        userKey: String,
        authMail: String,
        appName: String,
        inputTopicName: String,
        appID: Long,
        logType: LogFileTypes,
        logs: List<String>
    ): ResponseEntity<ApplicationResponse> {
        logService.processLogMessage(
            userKey,
            authMail,
            appName,
            inputTopicName,
            appID,
            logType,
            logs
        )
        return ResponseEntity(
            ApplicationResponse(
                type = "",
                title = "",
                instance = "",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()
            ),
            HttpStatus.OK
        )
    }

    @PostMapping("/{userKey}/{applicationName}/upload_file")
    fun uploadFile(
        authentication: Authentication,
        @PathVariable userKey: String,
        @PathVariable applicationName: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ApplicationResponse> {
        val user = userService.findByKey(userKey)
        val fileContent = file.inputStream.readBytes().toString(Charsets.UTF_8)
        val application = applicationService.findByUserAndName(user, applicationName)
        if (application.isPresent) {
            logService.processFileContent(authentication.name, application.get().id, fileContent, LogFileTypes.UNKNOWN_FORMAT)
        } else {
            applicationService.createApplication(applicationName, user) {
                logService.processFileContent(authentication.name, it.id, fileContent, LogFileTypes.UNKNOWN_FORMAT)
            }
        }
        return ResponseEntity(
            ApplicationResponse(
                type = "",
                title = "",
                instance = "",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()
            ),
            HttpStatus.OK
        )
    }

    @PostMapping("/{userKey}/sample_data")
    fun sampleData(
        authentication: Authentication,
        @PathVariable userKey: String
    ): ResponseEntity<ApplicationResponse> {
        val applicationNames = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")

        val user = try {
            userService.findByKey(userKey)
        } catch (e: NoSuchElementException) {
            logger.error("User key $userKey does not exist:", e)
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        // Internal routine to upload data
        fun uploadSampleData(app: Application) {
            logger.info("Uploading sample data for app $app")
            val fileContent = File("${resourcesPath}sample_data/${app.name}")
                .inputStream()
                .readBytes()
                .toString(Charsets.UTF_8)
            logService.processFileContent(authentication.name, app.id, fileContent, LogFileTypes.UNKNOWN_FORMAT)
        }

        for (appName in applicationNames) {
            try {
                val appOld = applicationService.findByUserAndName(user, appName)
                if (appOld.isPresent) {
                    applicationService.deleteApplication(appOld.get()) {
                        applicationService.createApplication(appName, user) {
                            uploadSampleData(it)
                        }
                    }
                } else {
                    applicationService.createApplication(appName, user) {
                        uploadSampleData(it)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error while creating sample app $appName", e)
            }
        }
        return ResponseEntity(
            ApplicationResponse(
                type = "",
                title = "",
                instance = "",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()
            ),
            HttpStatus.OK
        )
    }
}
