package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationAction
import com.loxbear.logsight.entities.enums.ApplicationStatus
import com.loxbear.logsight.repositories.ApplicationRepository
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpMethod
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.transaction.Transactional

@Service
class ApplicationService(
    val repository: ApplicationRepository,
    val kafkaService: KafkaService,
) {
    @Value("\${elasticsearch.username}")
    private lateinit var username: String

    @Value("\${elasticsearch.password}")
    private lateinit var password: String

    val applicationActiveListener = mutableMapOf<Long, ((Application) -> Unit)>()
    val applicationDeletedListener = mutableMapOf<Long, ((Unit) -> Unit)>()

    private val executor = Executors.newSingleThreadExecutor()
    val logger: Logger = LoggerFactory.getLogger(ApplicationService::class.java)

    @Value("\${kibana.url}")
    private lateinit var kibanaUrl: String

    @Value("\${resources.path}")
    private lateinit var resourcesPath: String

    fun createApplication(name: String, user: LogsightUser, callback: ((Application) -> Unit)): Application? {
        val application = _createApplication(name, user)
        application?.let {
            applicationActiveListener[it.id] = callback
            kafkaService.applicationChange(it, ApplicationAction.CREATE)
        }
        return application
    }

    fun createApplication(name: String, user: LogsightUser): Application? {
        val application = _createApplication(name, user)
        application?.let {
            kafkaService.applicationChange(it, ApplicationAction.CREATE)
        }
        return application
    }

    @Transactional
    protected fun _createApplication(name: String, user: LogsightUser): Application? {
        val applications = this.findAllByUser(user)
        applications.forEach {
            if (it.name == name) {
                return null
            }
        }

        // TODO this should go to frontend
        val p: Pattern = Pattern.compile("[^a-z0-9_]")
        val m: Matcher = p.matcher(name)
        val b: Boolean = m.find()
        if (b) {
            return null
        }
        val application =
            Application(id = 0, name = name, user = user, status = ApplicationStatus.CREATING, inputTopicName = "")
        logger.info("Creating application with name [{}] for user with id [{}]", name, user.id)
        try {
            repository.saveAndFlush(application)
        } catch (e: DataIntegrityViolationException) {
            return null
        }
        val request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }, " +
                "\"elasticsearch\": { \"cluster\" : [ ], " +
                "\"indices\" : [ {\"names\" : [${getApplicationIndicesForKibana(user)}]," +
                " \"privileges\" : [ \"all\" ]}] }, " +
                "\"kibana\": [ { \"base\": [], " +
                "\"feature\": { \"discover\": [ \"all\" ], \"dashboard\": [ \"all\" ] , \"advancedSettings\": [ \"all\" ], \"visualize\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, \"spaces\": [ \"kibana_space_${user.key}\" ] } ] }"
        )
        val restTemplate: RestTemplate = RestTemplateBuilder()
            .basicAuthentication(username, password)
            .build()
        restTemplate.put("$kibanaUrl/api/security/role/${user.key + "_" + user.email}", request)
        val requestDefaultIndex = UtilsService.createKibanaRequestWithHeaders(
            "{ \"value\": null}"
        )
//        logger.info("Set kibana default index for app $name")
//        restTemplate.postForEntity<String>(
//            "$kibanaUrl/s/kibana_space_${user.key}/api/index_patterns/default",
//            requestDefaultIndex
//        ).body!!
//        logger.info("Default index for app $name set successfully")
//        kafkaService.applicationChange(application, ApplicationAction.CREATE)
        return application
    }

    fun findAllByUser(user: LogsightUser): List<Application> = repository.findAllByUser(user)

    fun getApplicationIndexes(user: LogsightUser) =
        findAllByUser(user).joinToString(",") {
            "${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_ad"
        }

    fun getApplicationIndexesAgg(user: LogsightUser, application: Application?) =
        findAllByUser(user).filter {
            application?.let { application -> application.id == it.id } ?: true
        }.joinToString(",") { "${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_log_agg" }

    fun getApplicationIndicesForKibana(user: LogsightUser) =
//        findAllByUser(user).joinToString(",") {
//            "\"${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_parsing\", \"${
//                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
//            }_${it.name}_log_ad\", \"${
//                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
//            }_${it.name}_count_ad\", \"${
//                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
//            }_${it.name}_incidents\""
//        }
        findAllByUser(user).joinToString(",") {
            "\"${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_ad\", \"${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_count_ad\", \"${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_incidents\", \"${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_agg\", \"${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_quality\""
        }

    fun getApplicationIndexesForIncidents(user: LogsightUser, application: Application?) =
        findAllByUser(user).filter {
            application?.let { application -> application.id == it.id } ?: true
        }.joinToString(",") { "${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_incidents" }

    fun getApplicationIndexesForQuality(user: LogsightUser, application: Application?) =
        findAllByUser(user).filter {
            application?.let { application -> application.id == it.id } ?: true
        }.joinToString(",") {
            "${
            user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_quality"
        }

    fun getApplicationIndexesForLogCompare(user: LogsightUser, application: Application?, index: String) =
        findAllByUser(user).filter {
            application?.let { application -> application.id == it.id } ?: true
        }.joinToString(",") { "${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_$index" }

    @KafkaListener(topics = ["manager_settings_ack"], groupId = "1")
    fun applicationStatusControlListener(msg: String) {

        val jsonMsg = try {
            JSONObject(msg)
        } catch (e: JSONException) {
            logger.info("Failed to parse message $msg into JSON.", e)
            return
        }

        val ack = if (jsonMsg.has("ack")) jsonMsg.getString("ack") else ""
        when (ack) {
            "ACTIVE" -> {
                val applicationId = jsonMsg.getJSONObject("app").getLong("application_id")
                logger.info("Activating application with id $applicationId")
                repository.updateApplicationStatus(applicationId, ApplicationStatus.ACTIVE)
                val inputTopicName = jsonMsg
                    .getJSONObject("app")
                    .getJSONObject("input")
                    .getJSONObject("source")
                    .getString("topic")
                repository.updateTopicName(applicationId, inputTopicName)
                logger.info("Application topic updated")
                val application = findById(applicationId)
                application.ifPresent { applicationActiveListener[applicationId]?.invoke(it) }
                // Thread.sleep(10000)
            }
            "DELETED" -> {
                val applicationId = jsonMsg.getLong("app_id")
                repository.deleteById(applicationId)
                applicationDeletedListener[applicationId]?.invoke(Unit)
                logger.info("Application with id $applicationId successfully deleted")
            }
        }
    }

    fun findById(id: Long): Optional<Application> = repository.findById(id)

    fun findByUserAndName(user: LogsightUser, applicationName: String): Optional<Application> =
        repository.findByUserAndName(user, applicationName)

    fun deleteApplication(id: Long, callback: ((Unit) -> Unit)) =
        findById(id).ifPresent { deleteApplication(it, callback) }

    fun deleteApplication(id: Long) = findById(id).ifPresent { deleteApplication(it) }

    fun deleteApplication(app: Application, callback: ((Unit) -> Unit)) {
        applicationDeletedListener[app.id] = callback
        deleteApplication(app)
    }

    fun deleteApplication(app: Application) {
        logger.info("Set state of application $app to deleting")
        kafkaService.applicationChange(app, ApplicationAction.DELETE)
        executor.submit { deleteKibanaPatterns(app) }
    }

    fun deleteKibanaPatterns(application: Application) {
        val restTemplate: RestTemplate = RestTemplateBuilder()
            .basicAuthentication(username, password)
            .build()
        for (i in getApplicationIndicesForKibana(application.user).split(",")) {
            if (i.isNotEmpty() && i.contains(application.name)) {

                val indexPattern = i.replace("\\s|\"".toRegex(), "")
                val request = UtilsService.createKibanaRequestWithHeaders(
                    "{}"
                )
                try {
                    restTemplate.exchange<String>(
                        "$kibanaUrl/s/kibana_space_${application.user.key}/api/saved_objects/index-pattern/$indexPattern",
                        HttpMethod.DELETE,
                        request
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    fun updateKibanaPatterns(user: LogsightUser) {
        var indexPattern = ""
        var indexPatternAd = ""
        for (i in getApplicationIndicesForKibana(user).split(",")) {
            indexPattern = i.replace("\\s|\"".toRegex(), "")
            if (i.isNotEmpty() && (i.contains("incidents") || i.contains("log_ad"))) {
                if (i.contains("log_ad") && i.contains("fast_try_app")) {
                    indexPatternAd = i.replace("\\s|\"".toRegex(), "")
                }
                try {
                    val request = UtilsService.createKibanaRequestWithHeaders(
                        "{}"
                    )
                    val restTemplate: RestTemplate = RestTemplateBuilder()
                        .basicAuthentication(username, password)
                        .build()
                    restTemplate.exchange<String>(
                        "$kibanaUrl/s/kibana_space_${user.key}/api/saved_objects/index-pattern/$indexPattern",
                        HttpMethod.DELETE,
                        request
                    )
                } catch (e: Exception) {
                }

                val requestCreateIndexPattern = UtilsService.createKibanaRequestWithHeaders(
                    "{\"attributes\": { \"title\": \"$indexPattern\", \"timeFieldName\": \"@timestamp\"} }"
                )
                val restTemplate: RestTemplate = RestTemplateBuilder()
                    .basicAuthentication(username, password)
                    .build()
                restTemplate.postForEntity<String>(
                    "$kibanaUrl/s/kibana_space_${user.key}/api/saved_objects/index-pattern/$indexPattern",
                    requestCreateIndexPattern
                ).body!!
//                if (indexPattern.contains("log_ad")) {
//
//                }

                // curl -X POST "localhost:5601/api/saved_objects/_import" --form file=@export.ndjson -H 'kbn-xsrf: true' --user elastic:elasticsearchpassword
            }
        }

        val jsonString = UtilsService.readFileAsString("${resourcesPath}dashboards/export.ndjson")
        val jsonRequest = jsonString
            .replace("index_pattern_replace_log_ad", "$indexPatternAd")
            .replace(
                "index_pattern_replace_incidents",
                "${
                indexPatternAd.split("_").subList(0, indexPatternAd.split("_").size - 2)
                    .joinToString("_") + "_incidents"
                }"
            )
        File("file.ndjson").bufferedWriter().use { out ->
            out.write(jsonRequest)
        }

        val response: HttpResponse<String> =
            Unirest.post("$kibanaUrl/s/kibana_space_${user.key}/api/saved_objects/_import")
                .header("kbn-xsrf", "true")
                .basicAuth(username, password)
                .field("file", File("file.ndjson"))
                .asString()
    }
}
