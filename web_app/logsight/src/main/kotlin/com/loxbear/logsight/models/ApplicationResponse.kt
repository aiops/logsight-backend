package com.loxbear.logsight.models

import org.springframework.http.HttpStatus

data class ApplicationResponse(
    val description: String,
    val status: HttpStatus,
)