package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.enums.ApplicationStatus
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.models.log.LogFileType
import com.loxbear.logsight.models.log.LogMessage
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.LogService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.concurrent.Executors
import java.util.logging.Logger

@RestController
@RequestMapping("/api/logs")
class LogMessageController(
    val logService: LogService,
    val applicationService: ApplicationService,
    val userService: UserService
) {

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    val log: Logger = Logger.getLogger(LogMessageController::class.java.toString())
    private val executor = Executors.newSingleThreadExecutor()
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
        userKey: String, authMail: String, appName: String, inputTopicName: String, appID: Long, logType: LogFileTypes, logs: List<String>
    ): ResponseEntity<ApplicationResponse>{
        logService.processLogMessage(userKey,
            authMail,
            appName,
            inputTopicName,
            appID,
            logType,
            logs)
        return ResponseEntity(
            ApplicationResponse(
                type="",
                title="",
                instance="",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()), HttpStatus.OK
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
        val timeWait = 5000
        Thread.sleep(timeWait.toLong())
        val application = applicationService.findByUserAndName(user, applicationName).get()
        val fileContent = file.inputStream.readBytes()
        return uploadFile(authentication, application.id, fileContent.toString(Charsets.UTF_8), LogFileTypes.UNKNOWN_FORMAT)
    }

    @PostMapping("/{userKey}/sample_data")
    fun sampleData(
        authentication: Authentication,
        @PathVariable userKey: String
    ): ResponseEntity<ApplicationResponse> {
        val applicationNames = listOf<String>("hdfs_node", "name_node", "node_manager", "resource_manager")
        val user = userService.findByKey(userKey)
        val timeWait = 3000
        for (appName in applicationNames){
            try{
                val application = applicationService.findByUserAndName(user, appName).get()
                applicationService.deleteApplication(application.id)
                Thread.sleep(timeWait.toLong())
            }catch (e: Exception){
            }
            val application = applicationService.createApplication(appName, user)
            Thread.sleep(timeWait.toLong())
            val fileContent = File("${resourcesPath}sample_data/${appName}").inputStream().readBytes()
            executor.submit {
                if (application != null) {
                    uploadFile(authentication, application.id, fileContent.toString(Charsets.UTF_8), LogFileTypes.UNKNOWN_FORMAT)
                }
            }
        }
        Thread.sleep(timeWait.toLong()*5)
        return ResponseEntity(
            ApplicationResponse(
                type="",
                title="",
                instance="",
                detail = "Data uploaded successfully.",
                status = HttpStatus.OK.value()), HttpStatus.OK
        )

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