package ai.logsight.backend.security

object SecurityConstants {
    const val SECRET = "LogSightSecretKey2021"
    const val EXPIRATION_TIME: Long = 10000 // 10 days
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
}
