package ai.logsight.backend.users.ports.out.external

import ai.logsight.backend.users.domain.User
import org.springframework.stereotype.Service

@Service
class ExternalServiceManager(
    final val elasticsearch: ExternalElasticsearch
) {
    val services: Map<String, ExternalService> = mapOf("elasticsearch" to elasticsearch)
    fun initializeServicesForUser(user: User) {
        services.values.forEach { service ->
            service.initialize(user)
        }
    }

    fun teardownServicesForUser(user: User) {
        services.values.forEach { service ->
            service.teardown(user)
        }
    }
}
