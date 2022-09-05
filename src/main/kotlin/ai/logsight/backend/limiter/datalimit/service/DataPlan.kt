package ai.logsight.backend.limiter.datalimit.service

import ai.logsight.backend.users.domain.UserCategory
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

enum class DataPlan(private val bucketCapacity: Long) {
    FREEMIUM(450),
    DEVELOPER(450),
    CORPORATE(450);

    val limit: Bandwidth
        get() = Bandwidth.classic(
            bucketCapacity,
            Refill.intervally(bucketCapacity, Duration.ofMinutes(1))
        )
    companion object {
        fun resolveDataPlanFromUser(userType: UserCategory): DataPlan {
            return when (userType) {
                UserCategory.FREEMIUM -> FREEMIUM
                UserCategory.CORPORATE -> CORPORATE
                UserCategory.DEVELOPER -> DEVELOPER
            }
        }
    }
}
