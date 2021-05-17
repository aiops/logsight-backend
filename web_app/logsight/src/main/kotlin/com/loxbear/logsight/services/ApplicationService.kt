package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationStatus
import com.loxbear.logsight.repositories.ApplicationRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.lang.Exception
import javax.transaction.Transactional

@Service
class ApplicationService(val repository: ApplicationRepository, val kafkaService: KafkaService) {
    val logger = LoggerFactory.getLogger(ApplicationService::class.java)

    fun createApplication(name: String, user: LogsightUser): Application {
        val application = Application(id = 0, name = name, user = user, status = ApplicationStatus.IN_PROGRESS)
        logger.info("Creating application with name [{}] for user with id [{}]", name, user.id)
        repository.save(application)
        kafkaService.applicationCreated(application)
        return application
    }

    fun findAllByUser(user: LogsightUser): List<Application> = repository.findAllByUser(user)

    fun getApplicationIndexes(user: LogsightUser) =
        findAllByUser(user).joinToString(",") { "${user.key.toLowerCase().filter { it2 -> it2.isLetterOrDigit() }}_${it.name}_log_ad" }

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

    fun findById(id: Long): Application = repository.findById(id).orElseThrow { Exception("Application with id [$id] not found") }
}