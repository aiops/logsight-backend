package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.external.exceptions.ExternalServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ExternalServiceManager(
    val elasticsearch: ExternalElasticsearch,
    val services: Map<String, ExternalService> = mapOf("elasticsearch" to elasticsearch)
) {
    val logger: Logger = LoggerFactory.getLogger(ExternalServiceManager::class.java)

    @Throws(ExternalServiceException::class)
    fun initializeServicesForUser(user: User) {
        services.values.forEach { service ->
            service.initialize(user)
        }
    }

    @Throws(ExternalServiceException::class)
    fun teardownServicesForUser(user: User) {
        services.values.forEach { service ->
            service.teardown(user)
        }
    }
}
