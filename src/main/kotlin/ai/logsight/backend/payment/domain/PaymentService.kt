package ai.logsight.backend.payment.domain

import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.payment.config.PaymentConfigurationProperties
import ai.logsight.backend.payment.ports.out.web.responses.StripeCustomerPortalResponse
import ai.logsight.backend.payment.ports.out.web.responses.StripePaymentWithCheckoutResponse
import ai.logsight.backend.payment.ports.out.web.responses.StripeWebhookResponse
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.UserCategory
import ai.logsight.backend.users.extensions.toUserEntity
import ai.logsight.backend.users.ports.out.persistence.UserRepository
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.stripe.Stripe
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Customer
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.CustomerCreateParams
import com.stripe.param.checkout.SessionCreateParams
import org.json.JSONObject
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

@Service
class PaymentService(
    val userStorageService: UserStorageService,
    val paymentConfigurationProperties: PaymentConfigurationProperties,
    val commonConfigProperties: CommonConfigProperties,
    val userRepository: UserRepository
) {

    val mapper = ObjectMapper().registerModule(KotlinModule())!!
    private val logger = LoggerImpl(PaymentService::class.java)
    fun init() {
        Stripe.apiKey = paymentConfigurationProperties.stripeId
    }

    fun paymentWithCheckout(user: User, payment: CheckoutPayment): StripePaymentWithCheckoutResponse {
        init()
        val stripeCustomerID = getStripeCustomerId(user)
        val params: SessionCreateParams = SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setCustomer(stripeCustomerID)
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION).setSuccessUrl(payment.successUrl)
            .setCancelUrl(payment.cancelUrl)
            .addLineItem(SessionCreateParams.LineItem.builder().setPrice(payment.priceID).setQuantity(1).build())
            .build()
        val session: Session = Session.create(params)
        return StripePaymentWithCheckoutResponse(user.id, stripeCustomerID, session.id)
    }


    fun serveWebhook(request: HttpServletRequest, json: String): StripeWebhookResponse {
        init()
        val sigHeader: String = request.getHeader("Stripe-Signature")
        val endpointSecret = paymentConfigurationProperties.stripeWebhookSecretId
        val event = try {
            Webhook.constructEvent(json, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            throw SignatureVerificationException(e.message, sigHeader)
        }
        val stripeId = JSONObject(event.dataObjectDeserializer.rawJson).getString("customer")
        val user = userStorageService.findUserByStripeId(stripeId)

        when (event?.type) {
            "invoice.paid" -> {
                logger.info("Received [invoice.paid] for user ${user.id} with stripeId ${user.stripeId}")
                userStorageService.changeUserCategory(user.id, UserCategory.DEVELOPER)

            }

            "invoice.payment_failed" -> {
                logger.info("Received [invoice.payment_failed] for user ${user.id} with stripeId ${user.stripeId}")
                userStorageService.changeUserCategory(user.id, UserCategory.FREEMIUM)
            }

            "customer.subscription.deleted" -> {
                logger.info("Received [invoice.payment_failed] for user ${user.id} with stripeId ${user.stripeId}")
                userStorageService.changeUserCategory(user.id, UserCategory.FREEMIUM)
            }

            else -> {
            }
        }
        return StripeWebhookResponse(user.id, stripeId, event?.type)
    }

    fun getStripeCustomerPortal(user: User): StripeCustomerPortalResponse {
        init()
        val stripeCustomerID = getStripeCustomerId(user)
        val params = com.stripe.param.billingportal.SessionCreateParams.Builder()
            .setReturnUrl(commonConfigProperties.baseURL.toString())
            .setCustomer(stripeCustomerID)
            .build()
        val portalSession = com.stripe.model.billingportal.Session.create(params)
        return StripeCustomerPortalResponse(user.id, portalSession.url)
    }

    fun getStripeCustomerId(user: User): String {
        val stripeCustomerID = if (user.stripeId.isNullOrEmpty()) {
            logger.debug("First time payment for user ${user.id}, creating stripe ID.")
            val customerParams = CustomerCreateParams
                .builder()
                .setEmail(user.email)
                .build()
            val customer = Customer.create(customerParams)
            val userEntity = user.toUserEntity()
            userEntity.stripeId = customer.id
            userRepository.save(userEntity)
            customer.id
        } else {
            logger.debug("User ${user.id} already has stripeId")
            user.stripeId
        }
        return stripeCustomerID
    }

}