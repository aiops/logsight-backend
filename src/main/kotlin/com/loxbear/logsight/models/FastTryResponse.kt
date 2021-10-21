package com.loxbear.logsight.models

import org.springframework.web.multipart.MultipartFile

data class FastTryResponse(
    val id: Long,
    val key: String,
    val password: String,
    val kibanaPersonalUrl: String
)