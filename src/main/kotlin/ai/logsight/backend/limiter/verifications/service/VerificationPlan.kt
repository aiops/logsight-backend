package ai.logsight.backend.limiter.verifications.service

import ai.logsight.backend.users.domain.UserCategory
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

enum class VerificationPlan(private val bucketCapacity: Long) {
    FREEMIUM(2),
    DEVELOPER(5),
    CORPORATE(10);

    val limit: Bandwidth
        get() = Bandwidth.classic(
            bucketCapacity,
            Refill.intervally(bucketCapacity, Duration.ofMinutes(1))
        )
    companion object {
        fun resolveDataPlanFromUser(userType: UserCategory): VerificationPlan {
            return when (userType) {
                UserCategory.FREEMIUM -> FREEMIUM
                UserCategory.CORPORATE -> CORPORATE
                UserCategory.DEVELOPER -> DEVELOPER
            }
        }
    }
}
