package ai.logsight.backend.compare.out.rest.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.manager.compare")
@Component
class CompareRESTConfigProperties {
    val endpoint = "compare"
    val host = "localhost"
    val port = 5554
    val apiVersion = 1
}
