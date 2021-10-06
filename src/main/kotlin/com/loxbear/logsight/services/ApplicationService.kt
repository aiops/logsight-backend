package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationAction
import com.loxbear.logsight.entities.enums.ApplicationStatus
import com.loxbear.logsight.repositories.ApplicationRepository
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
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
        kafkaService.applicationChange(application, ApplicationAction.CREATE)
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
        val application = findById(id)
        logger.info("Deleting application with id [{}]", id)
        try {
            repository.delete(application)
        } catch (e: java.lang.Exception) {
            return false
        }
        kafkaService.applicationChange(application, ApplicationAction.DELETE)
        return true
    }
}