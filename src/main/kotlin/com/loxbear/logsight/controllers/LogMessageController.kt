package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.ApplicationResponse
import com.loxbear.logsight.models.ElasticsearchConfigRequest
import com.loxbear.logsight.models.log.LogMessage
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.LogService
import com.loxbear.logsight.services.UserService
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.multipart.MultipartFile
import utils.UtilsService
import java.io.File
import java.net.URI
import java.util.concurrent.Executors

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

    @PostMapping("/test_elasticsearch")
    fun testElasticsearch(@RequestBody elasticsearchConfigRequest: ElasticsearchConfigRequest): ResponseEntity<ApplicationResponse> {
        logger.info(
            "Testing elasticsearch connection on ${elasticsearchConfigRequest.elasticsearchUrl} " +
                "with index ${elasticsearchConfigRequest.elasticsearchIndex} for user " +
                elasticsearchConfigRequest.elasticsearchUser
        )
        try {
            val uri = URI(elasticsearchConfigRequest.elasticsearchUrl)
            elasticsearchTestConnect(
                uri,
                elasticsearchConfigRequest.elasticsearchIndex,
                elasticsearchConfigRequest.elasticsearchUser,
                elasticsearchConfigRequest.elasticsearchPassword,
                elasticsearchConfigRequest.elasticsearchTimestamp
            )
        } catch (e: Exception) {
            logger.warn(
                "Connection to elasticsearch on ${elasticsearchConfigRequest.elasticsearchUrl} " +
                    "failed. Reason: $e"
            )
            return ResponseEntity(
                ApplicationResponse(
                    type = "",
                    title = "Elasticsearch connection test",
                    status = 500,
                    detail = "Connection to elasticsearch on ${elasticsearchConfigRequest.elasticsearchUrl} " +
                        "failed. Reason: $e",
                    instance = ""
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
        return ResponseEntity(
            ApplicationResponse(
                type = "",
                title = "Elasticsearch connection test",
                status = 200,
                detail = "Elasticsearch connection test success!",
                instance = ""
            ),
            HttpStatus.OK
        )
    }

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
        val elasticsearchPeriod =
            JSONObject(requestBody).getLong("elasticsearchPeriod") * 1000 // from seconds to milliseconds
        val elasticsearchUser = JSONObject(requestBody).getString("elasticsearchUser")
        val elasticsearchPassword = JSONObject(requestBody).getString("elasticsearchPassword")
        val timestampKey = JSONObject(requestBody).getString("elasticsearchTimestamp")
        // TODO there is no tracking of successfully setup connections

        // restTemplate creation
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(elasticsearchUser, elasticsearchPassword)
            .build()

        val stopTime = JSONObject(requestBody).getString("elasticsearchEndTime")
        val startTime = JSONObject(requestBody).getString("elasticsearchStartTime")

        // try first query to check if connection is OK and if index exists
        val jsonString: String =
            UtilsService.readFileAsString("${resourcesPath}queries/get_all_data.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("timestamp_name", timestampKey)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        try {
            JSONObject(
                restTemplate.postForEntity<String>(
                    "$elasticsearchUrl/$elasticsearchIndex/_search",
                    request
                ).body!!
            ).getJSONObject("hits").getJSONArray("hits")
            // if connection and index exist (if this restTemplate call passes) then start getting the logs.
            executor.submit {
                getElasticLogs(
                    user.get(),
                    elasticsearchUrl,
                    elasticsearchIndex,
                    elasticsearchPeriod,
                    timestampKey,
                    restTemplate,
                    timestampKey,
                    startTime
                )
            }
            return ResponseEntity(
                ApplicationResponse(
                    type = "",
                    title = "Connection successful",
                    status = 1,
                    detail = "Connection set. Ingesting logs from elasticsearch to logsight.ai ...",
                    instance = ""
                ),
                HttpStatus.OK
            )
        } catch (e: Exception) {
            logger.error(e.message)
            // if the call fails then return failed message to check the parameters.
            return ResponseEntity(
                ApplicationResponse(
                    type = "Error",
                    title = "Connection failed.",
                    status = 404,
                    detail = "The elasticsearch URL and/or index are not found. Connection failed.",
                    instance = ""
                ),
                HttpStatus.NOT_FOUND
            )
        }
    }

    fun elasticsearchTestConnect(uri: URI, index: String, user: String, password: String, timestampKey: String) {
        // restTemplate creation
        val restTemplate = RestTemplateBuilder()
            .basicAuthentication(user, password)
            .build()
        val request = getQueryRequest("now-1h", "now", timestampKey)
        JSONObject(
            restTemplate.postForEntity<String>(
                "$uri/$index/_search",
                request
            ).body!!
        ).getJSONObject("hits").getJSONArray("hits")
        restTemplate.getForEntity(uri, String::class.java)
    }

    fun getQueryRequest(startTime: String, stopTime: String, timestampKey: String): HttpEntity<String> {
        val jsonString: String =
            UtilsService.readFileAsString("${resourcesPath}queries/get_all_data.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("timestamp_name", timestampKey)
        return UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
    }

    fun getElasticLogs(
        user: LogsightUser,
        elasticsearchUrl: String,
        elasticsearchIndex: String,
        elasticsearchPeriod: Long,
        elasticsearchTimestampName: String,
        restTemplate: RestTemplate,
        timestampKey: String,
        startTime: String
    ) {
        logger.info("Started elasticsearch polling thread.")
        // start to query with a period of 2 years
        val stopTime = "now"
        var startTimeVar = startTime
        val batchSize = 1000
        var currentSize = 0
        // i set this count as a threshold for how many times we wait without data before exiting the thread. Currently, set to 1 day.
        val waitingCount = if (elasticsearchPeriod > 0) 86400 / (elasticsearchPeriod / 1000) else 86400
        var countEmpty = 0
        val appLogMap: HashMap<String, ArrayList<LogMessage>> = hashMapOf()
        while (countEmpty < waitingCount) {
            appLogMap.clear()
            currentSize = 0
            logger.info("Executing polling query request.")
            while (true) {
                val data =
                    loadESData(restTemplate, elasticsearchUrl, elasticsearchIndex, startTimeVar, stopTime, timestampKey)
                currentSize += data.length()
                if (data.length() == 0) {
                    logger.info("No data received.")
                    countEmpty += 1 // if there is no data, increase the counter (this is when we have reach some threshold for the thread to finish)
                    break
                } else {
                    countEmpty = 0
                }
                logger.info("Received ${data.length()} log messages from elasticsearch.")

                val filteredData = data.filter { d ->
                    val log = JSONObject(d.toString())
                    startTimeVar = log.getJSONObject("_source").getString(elasticsearchTimestampName)
                    log.has("_source") && log.getJSONObject("_source").has("kubernetes")
                }
                logger.info("${data.length() - filteredData.size} log messages were dropped due to missing k8s meta-information.")

                val appLogPairs = filteredData.map { d ->
                    val log = JSONObject(d.toString()).getJSONObject("_source")
                    if (log.has("log")) {
                        log.put("message", log.getString("log"))
                        log.remove("log")
                    }

                    val k8sMeta = log.getJSONObject("kubernetes")
                    if (k8sMeta.has("container_image_id")) {
                        log.put("tag", k8sMeta.getString("container_image_id"))
                    } else {
                        log.put("tag", k8sMeta.getString("default"))
                    }

                    if (k8sMeta.has("container_name")) {
                        val appName = k8sMeta.getString("container_name")
                        val appNameClean: String = appName.toLowerCase().replace(Regex("[^a-z0-9]"), "")
                        Pair(appNameClean, log.toString())
                    } else {
                        Pair("default", log.toString())
                    }
                }
                logger.info("Mapped ${appLogPairs.size} application and log message pairs.")

                appLogPairs.forEach { appLog ->
                    if (!appLogMap.contains(appLog.first))
                        appLogMap[appLog.first] = arrayListOf()
                    appLogMap[appLog.first]?.add(LogMessage(appLog.second))
                }
                logger.info("Mapped ${appLogPairs.size} application and log message list.")

                if (data.length() < 1000 || currentSize >= batchSize)
                    break
            }

            if (currentSize == 0) {
                logger.info("No data received")
            } else {
                logger.info("$currentSize data objects received")
            }

            appLogMap.forEach { appLog ->
                val app = applicationService.findByUserAndName(user, appLog.key)
                if (app.isEmpty) {
                    applicationService.createApplication(appLog.key, user) { application ->
                        logService.logRepository.logToKafka(
                            user.key,
                            user.email,
                            application.name,
                            application.inputTopicName,
                            application.id,
                            LogFileTypes.UNKNOWN_FORMAT,
                            appLog.value
                        )
                    }
                } else {
                    logService.logRepository.logToKafka(
                        user.key,
                        user.email,
                        app.get().name,
                        app.get().inputTopicName,
                        app.get().id,
                        LogFileTypes.UNKNOWN_FORMAT,
                        appLog.value
                    )
                }
            }
            // sleep before polling again.
            if (currentSize < batchSize) {
                logger.info("Waiting until next polling period for ${elasticsearchPeriod / 1000} seconds.")
                Thread.sleep(elasticsearchPeriod)
            }
        }
    }

    fun loadESData(
        restTemplate: RestTemplate,
        elasticsearchUrl: String,
        elasticsearchIndex: String,
        startTime: String,
        stopTime: String,
        timestampKey: String
    ): JSONArray {
        val jsonString: String =
            UtilsService.readFileAsString("${resourcesPath}queries/get_all_data.json")
        val jsonRequest = jsonString.replace("start_time", startTime).replace("stop_time", stopTime)
            .replace("timestamp_name", timestampKey)
        val request = UtilsService.createElasticSearchRequestWithHeaders(jsonRequest)
        return JSONObject(
            restTemplate.postForEntity<String>(
                "$elasticsearchUrl/$elasticsearchIndex/_search",
                request
            ).body!!
        ).getJSONObject("hits").getJSONArray("hits")
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
        return sendLogsForProcessing(
            user.key,
            user.email,
            application.name,
            application.inputTopicName,
            application.id,
            LogFileTypes.UNKNOWN_FORMAT,
            logs
        )
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
            logService.processFileContent(
                authentication.name,
                application.get().id,
                fileContent,
                LogFileTypes.UNKNOWN_FORMAT
            )
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
