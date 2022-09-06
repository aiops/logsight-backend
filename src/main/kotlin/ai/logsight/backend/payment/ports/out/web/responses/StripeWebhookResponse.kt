package ai.logsight.backend.payment.ports.out.web.responses

import java.util.*

data class StripeWebhookResponse(
    val userId: UUID,
    val stripeId: String,
    val eventType: String?
)
