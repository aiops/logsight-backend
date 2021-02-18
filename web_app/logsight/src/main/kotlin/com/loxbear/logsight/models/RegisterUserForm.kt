package com.loxbear.logsight.models

data class RegisterUserForm(
    val email: String,

    val password: String,

    val repeatPassword: String
)