package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.models.log.LogMessage
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.LogService
import com.loxbear.logsight.services.UserService
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.multipart.MultipartFile
import utils.UtilsService
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.NoSuchElementException

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

    private val executor = Executors.newSingleThreadExecutor()

    val logger: org.slf4j.Logger = LoggerFactory.getLogger(ApplicationService::class.java)

    @PostMapping("/load_elasticsearch")
    fun loadElasticsearchLogs(
        authentication: Authentication,
        @RequestBody requestBody: String
    ): ResponseEntity<ApplicationResponse> {
        // get the user
        val user = userService.findByEmail(authentication.name)

        // get the request body variables needed for the elasticsearch connection and query
        val elasticsearchUrl = JSONObject(requestBody).getString("elasticsearchUrl")
        val elasticsearchIndex = JSONObject(requestBody).getString("elasticsearchIndex")
        val elasticsearchPeriod = JSONObject(requestBody).getLong("elasticsearchPeriod") * 1000 // from seconds to milliseconds
        val elasticsearchUser = JSONObject(requestBody).getString("elasticsearchUser")
        val elasticsearchPassword = JSONObject(requestBody).getString("elasticsearchPassword")
        // TODO there is no tracking of successfully setup connections

        // restTemplate creation
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(elasticsearchUser, elasticsearchPassword)
            .build()

        val stopTime = "now"
        val startTime = "now-1y"

        // try first query to check if connection is OK and if index exists
        val jsonString: String =
            UtilsService.readFileAsString("${resourcesPath}queries/get_all_data.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        try {
            JSONObject(
                restTemplate.postForEntity<String>(
                    "$elasticsearchUrl/$elasticsearchIndex/_search",
                    request
                ).body!!
            ).getJSONObject("hits").getJSONArray("hits")
            // if connection and index exist (if this restTemplate call passes) then start getting the logs.
            executor.submit { getElasticLogs(user.get(), elasticsearchUrl, elasticsearchIndex, elasticsearchPeriod, restTemplate) }
            return ResponseEntity(ApplicationResponse(type = "", title = "Connection successful", status = 1, detail = "Connection set. Ingesting logs from elasticsearch to logsight.ai ...", instance = ""), HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message)
            // if the call fails then return failed message to check the parameters.
            return ResponseEntity(ApplicationResponse(type = "Error", title = "Connection failed.", status = 404, detail = "The elasticsearch URL and/or index are not found. Connection failed.", instance = ""), HttpStatus.NOT_FOUND)
        }
    }

    fun getElasticLogs(user: LogsightUser, elasticsearchUrl: String, elasticsearchIndex: String, elasticsearchPeriod: Long, restTemplate: RestTemplate) {
        // start to query with a period of 2 years
        var stopTime = "now"
        var startTime = "now-2y"
        var data: JSONArray
        // i set this count as a threshold for how many times we wait without data before exiting the thread. Currently, set to 1 day.
        val waitingCount = 1440
        var countEmpty = 0
        while (countEmpty < waitingCount) {
            // query data
            val jsonString: String =
                UtilsService.readFileAsString("${resourcesPath}queries/get_all_data.json")
            val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
            data = JSONObject(
                restTemplate.postForEntity<String>(
                    "$elasticsearchUrl/$elasticsearchIndex/_search",
                    request
                ).body!!
            ).getJSONObject("hits").getJSONArray("hits")
            if (data.length() == 0) {
                countEmpty += 1 // if there is no data, increase the counter (this is when we have reach some threshold for the thread to finish)
                Thread.sleep(60000)
                continue
            }
            // send data to logsight python backend
            data.forEach {
                val log = JSONObject(it.toString()).getJSONObject("_source")
                try {
                    val applicationName = log.getString("container_name")
                    log.put("tag", log.getString("container_image_id"))
                    startTime = log.getString("@timestamp") // set the startTime to the timestamp of the last entry
                    val application = applicationService.findByUserAndName(user, applicationName).get()
                    if (application.equals(null)) {
                        applicationService.createApplication(applicationName, user) {
                            logger.info("Application was created")
                        }
                    }
                    logService.logRepository.logToKafka(
                        user.key, user.email, application.name, application.inputTopicName, application.id, LogFileTypes.UNKNOWN_FORMAT,
                        listOf(
                            LogMessage(log.toString())
                        )
                    )
                } catch (e: Exception) {
                    logger.error(e.message)
                }
            }
            // sleep before polling again.
            Thread.sleep(elasticsearchPeriod)
        }
    }

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
