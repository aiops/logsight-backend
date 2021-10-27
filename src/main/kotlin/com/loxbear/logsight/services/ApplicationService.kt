package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationAction
import com.loxbear.logsight.entities.enums.ApplicationStatus
import com.loxbear.logsight.repositories.ApplicationRepository
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.*
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.transaction.Transactional


@Service
class ApplicationService(
    val repository: ApplicationRepository,
    val kafkaService: KafkaService,
) {

    val logger: Logger = LoggerFactory.getLogger(ApplicationService::class.java)
    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @Value("\${resources.path}")
    private val resourcesPath: String = ""


    @Transactional
    fun createApplication(name: String, user: LogsightUser): Application? {
        val applications = this.findAllByUser(user)
        applications.forEach {
            if (it.name == name){
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
        val application = Application(id = 0, name = name, user = user, status = ApplicationStatus.IN_PROGRESS)
        logger.info("Creating application with name [{}] for user with id [{}]", name, user.id)
        try {
            repository.save(application)
        } catch (e: DataIntegrityViolationException) {
            return null
        }
        kafkaService.applicationChange(application, ApplicationAction.CREATE)
        val request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }, " +
                    "\"elasticsearch\": { \"cluster\" : [ ], " +
                    "\"indices\" : [ {\"names\" : [${getApplicationIndicesForKibana(user)}]," +
                    " \"privileges\" : [ \"all\" ]}] }, " +
                    "\"kibana\": [ { \"base\": [], " +
                    "\"feature\": { \"discover\": [ \"all\" ], \"dashboard\": [ \"all\" ] , \"advancedSettings\": [ \"all\" ], \"visualize\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, \"spaces\": [ \"kibana_space_${user.key}\" ] } ] }"
        )
        restTemplate.put("http://$kibanaUrl/kibana/api/security/role/${user.key + "_" + user.email}", request)

        val requestDefaultIndex = UtilsService.createKibanaRequestWithHeaders(
            "{ \"value\": null}"
        )
        restTemplate.postForEntity<String>(
            "http://$kibanaUrl/kibana/s/kibana_space_${user.key}/api/kibana/settings/defaultIndex",
            requestDefaultIndex
        ).body!!

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


    @KafkaListener(topics = ["container_settings_ack"])
    @Transactional
    fun applicationCreatedListener(message: String) {
        val response = JSONObject(message)
        val applicationId = response.get("application_id").toString().toLong()
        logger.info("Activating application with id [{}]", applicationId)
        repository.updateApplicationStatus(applicationId, ApplicationStatus.ACTIVE)
    }

    fun findById(id: Long): Application =
        repository.findById(id).orElseThrow { Exception("Application with id [$id] not found") }


    fun deleteApplication(id: Long): Boolean {
        logger.info("Deleting application with id [{}]", id)
        try {
            val application = findById(id)
            kafkaService.applicationChange(application, ApplicationAction.DELETE)
            for (i in getApplicationIndicesForKibana(application.user).split(",")) {
                if (i.isNotEmpty() && i.contains(application.name)) {

                    val indexPattern = i.replace("\\s|\"".toRegex(), "")
                    val request = UtilsService.createKibanaRequestWithHeaders(
                        "{}"
                    )
                    try {
                        restTemplate.exchange<String>("http://$kibanaUrl/kibana/s/kibana_space_${application.user.key}/api/saved_objects/index-pattern/$indexPattern", HttpMethod.DELETE, request)
                    }catch (e: Exception){

                    }
                }
            }
            repository.delete(application)
        } catch (e: java.lang.Exception){
            return false
        }

        return true
    }

    fun updateKibanaPatterns(user: LogsightUser) {
        var indexPattern = ""
        var indexPatternAd = ""
        for (i in getApplicationIndicesForKibana(user).split(",")) {
            indexPattern = i.replace("\\s|\"".toRegex(), "")
            if (i.isNotEmpty() && (i.contains("incidents") || i.contains("log_ad"))){
                if (i.contains("log_ad") && i.contains("fast_try_app")){
                    indexPatternAd = i.replace("\\s|\"".toRegex(), "")
                }
                try {
                    val request = UtilsService.createKibanaRequestWithHeaders(
                        "{}"
                    )
                    restTemplate.exchange<String>("http://$kibanaUrl/kibana/s/kibana_space_${user.key}/api/saved_objects/index-pattern/$indexPattern", HttpMethod.DELETE, request)

                }catch (e: Exception){
                }

                val requestCreateIndexPattern = UtilsService.createKibanaRequestWithHeaders(
                    "{\"attributes\": { \"title\": \"$indexPattern\", \"timeFieldName\": \"@timestamp\"} }"
                )
                restTemplate.postForEntity<String>(
                    "http://$kibanaUrl/kibana/s/kibana_space_${user.key}/api/saved_objects/index-pattern/$indexPattern",
                    requestCreateIndexPattern
                ).body!!
//                if (indexPattern.contains("log_ad")) {
//
//                }



                //curl -X POST "localhost:5601/kibana/api/saved_objects/_import" --form file=@export.ndjson -H 'kbn-xsrf: true' --user elastic:elasticsearchpassword


            }
        }

        val jsonString = UtilsService.readFileAsString("${resourcesPath}dashboards/export.ndjson")
        val jsonRequest = jsonString
            .replace("index_pattern_replace_log_ad", "$indexPatternAd")
            .replace("index_pattern_replace_incidents", "${indexPatternAd.split("_").subList(0,indexPatternAd.split("_").size-2).joinToString("_") + "_incidents"}")
        File("file.ndjson").bufferedWriter().use { out ->
            out.write(jsonRequest)
        }

        val response: HttpResponse<String> =
            Unirest.post("http://$kibanaUrl/kibana/s/kibana_space_${user.key}/api/saved_objects/_import")
                .header("kbn-xsrf", "true")
                .basicAuth("elastic", "elasticsearchpassword")
                .field("file", File("file.ndjson"))
                .asString()

    }
}