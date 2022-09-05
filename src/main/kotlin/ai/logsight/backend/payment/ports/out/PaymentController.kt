package ai.logsight.backend.payment.ports.out

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.payment.domain.CheckoutPayment
import ai.logsight.backend.payment.domain.PaymentService
import ai.logsight.backend.users.domain.UserCategory
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.stripe.Stripe
import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.Customer
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.CustomerCreateParams
import com.stripe.param.checkout.SessionCreateParams
import org.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    val userStorageService: UserStorageService,
    val paymentService: PaymentService
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!
    private val logger = LoggerImpl(PaymentController::class.java)

    fun init() {
        Stripe.apiKey =
            "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"
    }

    @Throws(StripeException::class)
    @PostMapping
    fun paymentWithCheckoutPage(@RequestBody payment: CheckoutPayment, authentication: Authentication): String? {
        init()
        val user = userStorageService.findUserByEmail(authentication.name)
        val stripeCustomerID: String

        if (user.stripeId == null){
            logger.debug("First time payment for user ${user.id}, creating stripe ID.")
            val customerParams = CustomerCreateParams
                .builder()
                .setEmail(payment.email)
                .build()
            val customer = Customer.create(customerParams)
            stripeCustomerID = customer.id
            paymentService.createCustomerId(user, customer.id)
        }else{
            logger.debug("User ${user.id} already has stripeId")
            stripeCustomerID = user.stripeId
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
                    SessionCreateParams.LineItem.builder()
                        .setPrice(payment.priceID)
                        .build()
                )
                .build()
            val session: Session = Session.create(params)
            val responseData: MutableMap<String, String> = HashMap()
            responseData["id"] = session.id
            return mapper.writeValueAsString(responseData)
        }else{
            val params: SessionCreateParams = SessionCreateParams.builder() // We use the credit card payment method
                .setCustomer(stripeCustomerID)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT).setSuccessUrl(payment.successUrl)
                .setCancelUrl(
                    payment.cancelUrl
                )
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(payment.priceID)
                        .build()
                )
                .build()
            val session: Session = Session.create(params)
            val responseData: MutableMap<String, String> = HashMap()
            responseData["id"] = session.id
            return mapper.writeValueAsString(responseData)
        }

    }


    @PostMapping("/webhook")
    fun webhook(request: HttpServletRequest, @RequestBody json: String): String? {
        init()
        logger.debug("Webhook received [{}]", json)
        val sigHeader: String = request.getHeader("Stripe-Signature")
        val endpointSecret = "whsec_brxduRlHbT0O9Z6EooHI0Ps2J4EQqDxQ"

        val event = try {
            Webhook.constructEvent(json, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            return ""
        }

        val stripeId = JSONObject(event.dataObjectDeserializer.rawJson).getString("customer")
        val user = userStorageService.findUserByStripeId(stripeId)

        when (event?.type) {
            "invoice.paid" -> {
                logger.info("Received [invoice.paid] for user ${user.id} with stripeId ${user.stripeId}")
                val product = JSONObject(JSONObject(event.dataObjectDeserializer.rawJson)
                    .getJSONObject("lines")
                    .getJSONArray("data")[0])
                    .getJSONObject("price")
                    .getString("product")
                if (product == "prod_MNOMaFyn9XZc8r"){
                    paymentService.changeUserCategory(user,  UserCategory.DEVELOPER)
                }
            }
            "invoice.payment_failed" -> {
                logger.info("Received [invoice.payment_failed] for user ${user.id} with stripeId ${user.stripeId}")
                paymentService.changeUserCategory(user, UserCategory.FREEMIUM)
            }
            "customer.subscription.deleted" -> {
                logger.info("Received [invoice.payment_failed] for user ${user.id} with stripeId ${user.stripeId}")
                paymentService.changeUserCategory(user, UserCategory.FREEMIUM)
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
        val user = userStorageService.findUserByEmail(authentication.name)
        val stripeCustomerID = user.stripeId
        val domainUrl = "http://localhost:4200"

        val params = com.stripe.param.billingportal.SessionCreateParams.Builder()
            .setReturnUrl(domainUrl)
            .setCustomer(stripeCustomerID)
            .build()
        val portalSession = com.stripe.model.billingportal.Session.create(params)
        val responseData: MutableMap<String, Any> = HashMap()
        responseData["url"] = portalSession.url
        return mapper.writeValueAsString(responseData)
    }
}