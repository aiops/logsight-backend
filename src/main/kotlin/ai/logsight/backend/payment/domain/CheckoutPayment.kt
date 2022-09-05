package ai.logsight.backend.payment.domain

data class CheckoutPayment(
    val name: String,
    val currency: String,
    val successUrl: String,
    val cancelUrl: String,
    val email: String,
    val priceID: String,
    val subscription: Boolean
)