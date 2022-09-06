package ai.logsight.backend.payment.exceptions

class InvalidPaymentMethodException(override val message: String? = null) : RuntimeException(message)
