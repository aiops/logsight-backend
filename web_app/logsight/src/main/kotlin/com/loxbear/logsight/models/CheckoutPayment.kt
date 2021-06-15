package com.loxbear.logsight.models

data class CheckoutPayment(
    val name: String,
    val currency: String,
    val successUrl: String,
    val cancelUrl: String,
    val amount: Long,
    val quantity: Long
)