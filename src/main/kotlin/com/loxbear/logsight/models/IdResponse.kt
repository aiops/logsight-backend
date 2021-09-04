package com.loxbear.logsight.models

import org.springframework.http.HttpStatus

data class IdResponse(
    val description: String,
    val status: HttpStatus,
    val id: Long?
    )