package ai.logsight.backend.payment.domain

data class CheckoutPayment(
    val currency: String,
    val email: String,
    val priceID: String,
    val successUrl: String,
    val cancelUrl: String
)