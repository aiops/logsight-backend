package com.loxbear.logsight.controllers

import com.google.gson.Gson
import com.loxbear.logsight.models.CheckoutPayment
import com.loxbear.logsight.services.UsersService
import com.stripe.Stripe
import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/api/payments")
class PaymentController {
    val logger = LoggerFactory.getLogger(PaymentController::class.java)
    private val gson = Gson()

    fun init() {
        Stripe.apiKey =
            "sk_test_51ILUOvIf2Ur5sxpS0QXryzoYLNXCXD1DFJ1EUBcvoLkwrMxLQ9ijx7aXW1cq5x2z7Syf9MRp1RgMzO9n2eNzD5V200t826qw4t"
    }

    @Throws(StripeException::class)
    @PostMapping
    fun paymentWithCheckoutPage(@RequestBody payment: CheckoutPayment): String? {
        logger.info("Payment subscription [{}]", payment)
        init()
        val params: SessionCreateParams = SessionCreateParams.builder() // We will use the credit card payment method
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION).setSuccessUrl(payment.successUrl)
            .setCancelUrl(
                payment.cancelUrl
            )
            .addLineItem(
                SessionCreateParams.LineItem.builder().setQuantity(payment.quantity)
                    .setPrice(payment.priceID)
                    .build()
            )
            .build()
        val session: Session = Session.create(params)
        val responseData: MutableMap<String, String> = HashMap()
        responseData["id"] = session.id
        return gson.toJson(responseData)
    }

    @PostMapping("/webhook")
    fun webhook(request: HttpServletRequest, @RequestBody json: String): String? {
        logger.info("Webhook [{}]", json)
        val sigHeader: String = request.getHeader("Stripe-Signature")
        val endpointSecret: String = "STRIPE_WEBHOOK_SECRET"

        val event = try {
            Webhook.constructEvent(json, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            // Invalid signature
            return ""
        }

        when (event?.type) {
            "checkout.session.completed" -> {
            }
            "invoice.paid" -> {
            }
            "invoice.payment_failed" -> {
            }
            else -> {
            }
        }

        return ""
    }


    //not working for now
    @PostMapping("/customer_portal/{id}")
    fun customerPortal(@PathVariable id: String): String? {
        val customer = id
        val domainUrl = "http://localhost:4200"

        val params = com.stripe.param.billingportal.SessionCreateParams.Builder()
            .setReturnUrl(domainUrl)
            .setCustomer(customer)
            .build()
        val portalsession = com.stripe.model.billingportal.Session.create(params)
        val responseData: MutableMap<String, Any> = HashMap()
        responseData["url"] = portalsession.url
        return gson.toJson(responseData)
    }
}