package ai.logsight.backend.payment.ports.out.web

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.payment.domain.CheckoutPayment
import ai.logsight.backend.payment.domain.PaymentService
import ai.logsight.backend.payment.ports.out.web.responses.StripeCustomerPortalResponse
import ai.logsight.backend.payment.ports.out.web.responses.StripePaymentWithCheckoutResponse
import ai.logsight.backend.payment.ports.out.web.responses.StripeWebhookResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.stripe.exception.StripeException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    val userStorageService: UserStorageService,
    val paymentService: PaymentService
) {

    @Throws(StripeException::class)
    @PostMapping
    fun paymentWithCheckoutPage(@RequestBody payment: CheckoutPayment, authentication: Authentication): StripePaymentWithCheckoutResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return paymentService.paymentWithCheckout(user, payment)
    }

    @PostMapping("/webhook")
    fun webhook(request: HttpServletRequest, @RequestBody json: String): StripeWebhookResponse {
        return paymentService.serveWebhook(request, json)
    }

    @PostMapping("/customer_portal")
    fun customerPortal(authentication: Authentication): StripeCustomerPortalResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return paymentService.getStripeCustomerPortal(user)
    }
}