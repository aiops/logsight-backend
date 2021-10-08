package com.loxbear.logsight.controllers

import com.google.gson.Gson
import com.loxbear.logsight.models.CheckoutPayment
import com.loxbear.logsight.services.KafkaService
import com.loxbear.logsight.services.PaymentService
import com.loxbear.logsight.services.UserService
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
    val userService: UserService,
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
        val userOpt = userService.findByEmail(authentication.name)
        logger.info("Received new subscription request from user [{}] with payment details [{}]", userOpt, payment)
        var stripeCustomerID: String
        return userOpt.map { user ->
            if (user.stripeCustomerId == null) {
                logger.info("User does not exist, therefore, creating user with ID [{}]", user.stripeCustomerId)
                val customerParams = CustomerCreateParams
                    .builder()
                    .setEmail(payment.email)
                    .build()
                logger.info("Creating stripe customer params")
                val customer = Customer.create(customerParams)
                stripeCustomerID = customer.id
                logger.info("Stripe customer is created with stripeCustomerID= [{}]", stripeCustomerID)
                paymentService.createCustomerId(user, customer.id)
            } else {
                stripeCustomerID = user.stripeCustomerId
                logger.info("The customer already exists with stripeCustomerID= [{}]", stripeCustomerID)
            }
            if (payment.subscription) {
                logger.info("The payment will be in subscription mode")
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
                logger.info("Creating stripe subscription session!")
                val session: Session = Session.create(params)
                val responseData: MutableMap<String, String> = HashMap()
                responseData["id"] = session.id
                logger.info("Subscription session successfully created with sessionID = [{}]", session.id)
                gson.toJson(responseData)
            } else {
                logger.info("The payment will be in OTP mode!")
                val params: SessionCreateParams =
                    SessionCreateParams.builder() // We will use the credit card payment method
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
                logger.info("Creating stripe OTP session!")
                val session: Session = Session.create(params)
                val responseData: MutableMap<String, String> = HashMap()
                responseData["id"] = session.id
                logger.info("OTP Session successfully created with sessionID = [{}]", session.id)
                gson.toJson(responseData)
            }
        }.orElse(null)
    }


    @PostMapping("/webhook")
    fun webhook(request: HttpServletRequest, @RequestBody json: String): String? {
        init()
        logger.info("Webhook from stripe is received with the following details: [{}]", json)
        val sigHeader: String = request.getHeader("Stripe-Signature")
        val endpointSecret = "whsec_oDJqklbPr9Dg90UBsnTbhHxvGRLbLye4"

        val event = try {
            Webhook.constructEvent(json, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            return ""
        }

        logger.info("Event was successfully constructed [{}]", event)
        val customerId = JSONObject(event.dataObjectDeserializer.rawJson).getString("customer")
        val user = userService.findByStripeCustomerID(customerId)

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
                logger.info(event.dataObjectDeserializer.rawJson.toString())
                logger.info(event.dataObjectDeserializer.rawJson)
                val data = JSONObject(event.dataObjectDeserializer.rawJson).getJSONObject("lines").getJSONArray("data")[0]
                logger.info("Payment data obtained from stripe webhook")
                val quantity = JSONObject(data.toString()).getLong("quantity")
                logger.info("Quantity is successfully obtained from the stripe webhook")
                logger.info(quantity.toString())
                var availableData = 0L
                paymentService.resetAvailableAndUsedData(user)
                if (!user.hasPaid){
                    availableData = quantity*1000000000
                }else{
                    availableData = quantity*1000000000 + user.availableData
                }
                logger.info("The previously availableData of the user [{}] was [{}] B, now it is updated to [{}] B", user.availableData.toString(), availableData.toString())
                paymentService.paymentSuccessful(user, customerId, availableData)
                logger.info("The database was updated with the newest details for the available data for the user with ID: [{}]", user.id)
                kafkaService.updatePayment(user.key, true)
                logger.info("Updated payment in kafka")
                paymentService.updateLimitApproaching(user, false)
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

    @PostMapping("/customer_portal")
    fun customerPortal(authentication: Authentication): String? {
        init()
        val userOpt = userService.findByEmail(authentication.name)
        return userOpt.map { user ->
            val stripeCustomerID = user.stripeCustomerId
            val domainUrl = "https://demo.logsight.ai/"

            val params = com.stripe.param.billingportal.SessionCreateParams.Builder()
                .setReturnUrl(domainUrl)
                .setCustomer(stripeCustomerID)
                .build()
            val portalsession = com.stripe.model.billingportal.Session.create(params)
            val responseData: MutableMap<String, Any> = HashMap()
            responseData["url"] = portalsession.url
            gson.toJson(responseData)
        }.orElse(null)
    }
}