package ai.logsight.backend.payment.ports.out.web.responses

import java.util.*

data class StripePaymentWithCheckoutResponse(
    val userId: UUID,
    val stripeId: String,
    val sessionId: String
)
