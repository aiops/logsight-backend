package ai.logsight.backend.common.config

import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import javax.validation.constraints.Email

@ConstructorBinding
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "logsight.common")
data class CommonConfigProperties(
    @URL val baseURL: java.net.URI,
    @Email val logsightEmail: String,
)
