package ai.logsight.backend.application.ports.out.rpc.adapters.rest.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.manager.app-rpc")
@Component
class AnalyticsManagerRESTConfigurationProperties {
    val host = "localhost"
    val port = 5000
    val apiVersion = 1.0
    val createPath = "apps/create"
    val deletePath = "apps/delete"
}
