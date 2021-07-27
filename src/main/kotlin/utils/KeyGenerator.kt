package utils

import java.security.SecureRandom
import java.util.*

object KeyGenerator {
    val secureRandom = SecureRandom()
    val encoder = Base64.getUrlEncoder().withoutPadding()
    val SIZE = 20

    fun generate(): String {
        val buffer = ByteArray(SIZE)
        secureRandom.nextBytes(buffer)
        return encoder.encodeToString(buffer).toLowerCase().filter { it.isLetterOrDigit()}
    }
}