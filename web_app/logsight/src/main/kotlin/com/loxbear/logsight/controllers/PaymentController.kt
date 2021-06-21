package com.loxbear.logsight.controllers

import com.google.gson.Gson
import com.loxbear.logsight.models.CheckoutPayment
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.PaymentService
import com.loxbear.logsight.services.UsersService
import com.stripe.Stripe
import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/api/payments")
class PaymentController(
    val usersService: UsersService,
    val paymentService: PaymentService,
    val kafkaService: KafkaService
) {
    val logger = LoggerFactory.getLogger(PaymentController::class.java)
    private val gson = Gson()

    fun init() {
        Stripe.apiKey =
            "sk_test_51ILUOvIf2Ur5sxpS0QXryzoYLNXCXD1DFJ1EUBcvoLkwrMxLQ9ijx7aXW1cq5x2z7Syf9MRp1RgMzO9n2eNzD5V200t826qw4t"
    }

    @Throws(StripeException::class)
    @PostMapping
    fun paymentWithCheckoutPage(@RequestBody payment: CheckoutPayment): String? {
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
        init()
        logger.info("Webhook [{}]", json)
        val sigHeader: String = request.getHeader("Stripe-Signature")
        val endpointSecret: String = "whsec_oDJqklbPr9Dg90UBsnTbhHxvGRLbLye4"

        val event = try {
            Webhook.constructEvent(json, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            // Invalid signature
            return ""
        }

        val customerId = JSONObject(event.dataObjectDeserializer.rawJson).getString("customer")
        val customerEmail = JSONObject(event.dataObjectDeserializer.rawJson).getString("customer_email")
        val user = usersService.findByEmail(customerEmail)

        when (event?.type) {

            "customer.created" -> {
                //add the customer id to the user table
            }

            "checkout.session.completed" -> {
                // add the costumer
            }
            "invoice.paid" -> {
                logger.info("Received [invoice.paid] for user [{}] with stripeCustomerId [{}]", user, customerId)
                paymentService.paymentSuccessful(user, customerId)
                kafkaService.updatePayment(user.key, true)
                // add is_active_subscription = 1 in user table
                // and send on kafka topic to enable sending of data
            }
            "invoice.payment_failed" -> {
                logger.info("Received [invoice.payment_failed] for user [{}] with stripeCustomerId [{}]", user, customerId)
                paymentService.updateHasPaid(user, false)
                kafkaService.updatePayment(user.key, false)
                // add is_active_subscription = 0 in user table
                // and send on kafka topic to forbid sending of data
            }
            else -> {
            }
        }

        return ""
    }


    //not working completely for now, costumer ID should be added here
    @PostMapping("/customer_portal/{id}")
    fun customerPortal(@PathVariable id: String, authentication: Authentication): String? {
        init()
        val user = usersService.findByEmail(authentication.name)
        println("USER:")
        println(user)
        val customer = "cus_JgRuT7L6zbjtic"
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