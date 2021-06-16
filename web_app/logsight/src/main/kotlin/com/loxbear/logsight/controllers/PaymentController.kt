package com.loxbear.logsight.controllers

import com.google.gson.Gson
import com.loxbear.logsight.models.CheckoutPayment
import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/payments")
class PaymentController {

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
}