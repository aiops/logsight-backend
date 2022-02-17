package ai.logsight.backend.verification.out.rest.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.manager.verification")
@Component
class VerificationRESTConfigProperties {
    val endpoint = "verification"
    val host = "localhost"
    val port = 5554
    val apiVersion = 1
}
