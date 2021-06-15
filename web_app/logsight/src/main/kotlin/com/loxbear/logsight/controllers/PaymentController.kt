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
            "sk_test_51J2hYvL4BgW4lbGurDXQe6WalAumK7iPH3e8Y1nUHOEQ7PFQKuHItA47NsWTFyczGusKCJ64jDgrmV1aYwwRKOHU00TRTO9Rg5"
    }

    @Throws(StripeException::class)
    @PostMapping
    fun paymentWithCheckoutPage(@RequestBody payment: CheckoutPayment): String? {
        init()
        val params: SessionCreateParams = SessionCreateParams.builder() // We will use the credit card payment method
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setMode(SessionCreateParams.Mode.PAYMENT).setSuccessUrl(payment.successUrl)
            .setCancelUrl(
                payment.cancelUrl
            )
            .addLineItem(
                SessionCreateParams.LineItem.builder().setQuantity(payment.quantity)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(payment.currency).setUnitAmount(payment.amount)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData
                                    .builder().setName(payment.name).build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
        val session: Session = Session.create(params)
        val responseData: MutableMap<String, String> = HashMap()
        responseData["id"] = session.id
        return gson.toJson(responseData)
    }
}