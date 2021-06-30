package com.loxbear.logsight.controllers

import com.google.gson.Gson
import com.loxbear.logsight.models.CheckoutPayment
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.PaymentService
import com.loxbear.logsight.services.UsersService
import com.stripe.Stripe
import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.Customer
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.CustomerCreateParams
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
    fun paymentWithCheckoutPage(@RequestBody payment: CheckoutPayment, authentication: Authentication): String? {
        init()
        val user = usersService.findByEmail(authentication.name)
        val stripeCustomerID: String

        if (user.stripeCustomerId == null){
            println("Creating user!")
            println(user.stripeCustomerId)
            val customerParams = CustomerCreateParams
                .builder()
                .setEmail(payment.email)
                .build()
            val customer = Customer.create(customerParams)
            stripeCustomerID = customer.id
            paymentService.createCustomerId(user, customer.id)
        }else{
            println("User exists:")
            println(user.stripeCustomerId)
            stripeCustomerID = user.stripeCustomerId
        }
        if (payment.subscription){
            val params: SessionCreateParams = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setCustomer(stripeCustomerID)
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
        }else{
            val params: SessionCreateParams = SessionCreateParams.builder() // We will use the credit card payment method
                .setCustomer(stripeCustomerID)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT).setSuccessUrl(payment.successUrl)
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

    }


    @PostMapping("/webhook")
    fun webhook(request: HttpServletRequest, @RequestBody json: String): String? {
        init()
        logger.info("Webhook [{}]", json)
        val sigHeader: String = request.getHeader("Stripe-Signature")
        val endpointSecret = "whsec_oDJqklbPr9Dg90UBsnTbhHxvGRLbLye4"

        val event = try {
            Webhook.constructEvent(json, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            return ""
        }

        val customerId = JSONObject(event.dataObjectDeserializer.rawJson).getString("quantity")
        val user = usersService.findByStripeCustomerID(customerId)

        when (event?.type) {

//            "customer.created" -> {
//                //add the customer id to the user table
//            }
//
//            "checkout.session.completed" -> {
//                // add the costumer
//            }
            "invoice.paid" -> {
                logger.info("Received [invoice.paid] for user [{}] with stripeCustomerId [{}]", user, customerId)
                val data = JSONObject(event.dataObjectDeserializer.rawJson).getJSONObject("data").getJSONObject("object").getJSONObject("lines").getJSONArray("data")[0]
                val quantity = JSONObject(data).getLong("quantity")

                val availableData = quantity*1000000000 + user.availableData - user.usedData
                paymentService.paymentSuccessful(user, customerId, availableData)
                kafkaService.updatePayment(user.key, true)
            }
            "invoice.payment_failed" -> {
                logger.info("Received [invoice.payment_failed] for user [{}] with stripeCustomerId [{}]", user, customerId)
                paymentService.updateHasPaid(user, false)
                kafkaService.updatePayment(user.key, false)
            }
            else -> {
            }
        }

        return ""
    }


    //not working completely for now, costumer ID should be added here
    @PostMapping("/customer_portal")
    fun customerPortal(authentication: Authentication): String? {
        init()
        val user = usersService.findByEmail(authentication.name)
        val stripeCustomerID = user.stripeCustomerId
        val domainUrl = "http://localhost:4200"

        val params = com.stripe.param.billingportal.SessionCreateParams.Builder()
            .setReturnUrl(domainUrl)
            .setCustomer(stripeCustomerID)
            .build()
        val portalsession = com.stripe.model.billingportal.Session.create(params)
        val responseData: MutableMap<String, Any> = HashMap()
        responseData["url"] = portalsession.url
        return gson.toJson(responseData)
    }
}