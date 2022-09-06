package ai.logsight.backend.payment.ports.out.web.responses

import java.util.*

data class StripeCustomerPortalResponse(
    val userId: UUID,
    val stripeCustomerPortalUrl: String
)
