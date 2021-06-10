package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationAction
import com.loxbear.logsight.entities.enums.ApplicationStatus
import com.loxbear.logsight.repositories.ApplicationRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import utils.UtilsService
import utils.UtilsService.Companion.createElasticSearchRequestWithHeaders
import java.lang.Exception
import java.lang.RuntimeException
import javax.transaction.Transactional

@Service
class ApplicationService(val repository: ApplicationRepository, val kafkaService: KafkaService) {
    val logger = LoggerFactory.getLogger(ApplicationService::class.java)
    val restTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build();

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    fun createApplication(name: String, user: LogsightUser): Application {
        val application = Application(id = 0, name = name, user = user, status = ApplicationStatus.IN_PROGRESS)
        logger.info("Creating application with name [{}] for user with id [{}]", name, user.id)
        repository.save(application)
        kafkaService.applicationChange(application, ApplicationAction.CREATE)
        println("APPlications")
        println(getApplicationIndexes(user))
        val request = UtilsService.createKibanaRequestWithHeaders(
            "{ \"metadata\" : { \"version\" : 1 }, " +
                "\"elasticsearch\": { \"cluster\" : [ ], " +
                "\"indices\" : [ {\"names\" : [${getApplicationIndicesForKibana(user)}]," +
                " \"privileges\" : [ \"all\" ]}] }, " +
                "\"kibana\": [ { \"base\": [], " +
                "\"feature\": { \"discover\": [ \"all\" ], " +
                "\"logs\":[ \"all\" ], " +
                "\"indexPatterns\": [ \"all\" ] }, \"spaces\": [ \"kibana_space_${user.key}\" ] } ] }"
        )
        restTemplate.put("http://$kibanaUrl/kibana/api/security/role/kibana_role_${user.key}", request)
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
        findAllByUser(user).joinToString(",") {
            "\"${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_parsing\", \"${
                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_log_ad\", \"${
                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_count_ad\", \"${
                user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }
            }_${it.name}_incidents\""
        }


    fun getApplicationIndexesForIncidents(user: LogsightUser, application: Application?) =
        findAllByUser(user).filter {
            application?.let { application -> application.id == it.id } ?: true
        }.joinToString(",") { "${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_incidents" }

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

    fun deleteApplication(id: Long) {
        val application = findById(id)
        logger.info("Deleting application with id [{}]", id)
        repository.delete(application)
        kafkaService.applicationChange(application, ApplicationAction.DELETE)
    }
}