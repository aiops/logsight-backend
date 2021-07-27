package com.loxbear.logsight.security

object SecurityConstants {
    const val SECRET = "LogSightSecretKey2021"
    const val EXPIRATION_TIME: Long = 864000000 // 10 days
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
}