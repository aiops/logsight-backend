package com.loxbear.logsight.models

data class UserModel(
    val id: Long,
    val email: String,
    val key: String,
    val activated: Boolean,
)