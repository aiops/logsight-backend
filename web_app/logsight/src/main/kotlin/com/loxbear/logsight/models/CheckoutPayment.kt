package com.loxbear.logsight.models

data class CheckoutPayment(
    val name: String,
    val currency: String,
    val successUrl: String,
    val cancelUrl: String,
    val amount: Long,
    val email: String,
    val quantity: Long,
    val priceID: String,
    val subscription: Boolean
)