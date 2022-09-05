package ai.logsight.backend.security

object SecurityConstants {
    // TODO: 30.08.22 remove hardcoded secret
    const val SECRET = "LogSightSecretKey2021"
    const val EXPIRATION_TIME: Long = 864000000 // 10 days
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
}
