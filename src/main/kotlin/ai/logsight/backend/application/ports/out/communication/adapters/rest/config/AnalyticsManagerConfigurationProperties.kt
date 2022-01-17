package ai.logsight.backend.application.ports.out.communication.adapters.rest.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "logsight.manager")
@Component
class AnalyticsManagerConfigurationProperties {
    var host = "localhost"
    var port = 5000
    var apiVersion = 1.0
    var createPath = "apps/create"
    var deletePath = "apps/delete"
}
