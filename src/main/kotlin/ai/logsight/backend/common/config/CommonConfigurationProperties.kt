package ai.logsight.backend.common.config

import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.validation.constraints.Email

@ConstructorBinding
@ConfigurationProperties(prefix = "common-config")
data class CommonConfigurationProperties(
    @URL
    val baseURL: java.net.URI,
    @Email
    val logsightEmail: String
)