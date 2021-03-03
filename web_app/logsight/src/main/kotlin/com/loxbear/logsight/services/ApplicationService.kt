package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.repositories.ApplicationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApplicationService(val repository: ApplicationRepository, val kafkaService: KafkaService) {
    val logger = LoggerFactory.getLogger(ApplicationService::class.java)

    fun createApplication(name: String, user: LogsightUser): Application {
        val application = Application(id = 0, name = name, user = user)
        logger.info("Creating application with name [{}] for user with id [{}]", name, user.id)
        repository.save(application)
        kafkaService.applicationCreated(application)
        return application
    }

}