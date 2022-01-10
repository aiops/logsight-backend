package ai.logsight.backend.email.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "email")
class EmailConfigurationProperties {
    var host: String = ""
    var port: Int = 0
    var username: String = ""
    var password: String = ""
}
