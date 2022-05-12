package ai.logsight.backend.security

import java.security.SecureRandom
import java.util.*

object KeyGenerator {
    private val secureRandom = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private const val size = 20

    fun generate(): String {
        val buffer = ByteArray(size)
        secureRandom.nextBytes(buffer)
        return encoder.encodeToString(buffer).lowercase(Locale.getDefault()).filter { it.isLetterOrDigit() }
    }
}
