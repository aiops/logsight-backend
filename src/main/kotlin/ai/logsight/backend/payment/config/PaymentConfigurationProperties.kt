package ai.logsight.backend.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "logsight.payment")
@EnableConfigurationProperties
data class PaymentConfigurationProperties(
    var stripeId: String,
    var stripeWebhookSecretId: String,
    var stripeProductId: String
)
