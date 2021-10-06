package com.loxbear.logsight.models

import org.springframework.http.HttpStatus

data class ApplicationResponse(
    val type: String,
    val title: String,
    val status: String,
    val detail: String,
    val instance: String
)