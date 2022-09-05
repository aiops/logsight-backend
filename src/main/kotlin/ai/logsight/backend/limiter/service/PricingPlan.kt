package ai.logsight.backend.limiter.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

enum class PricingPlan(private val bucketCapacity: Int) {
    FREE(2),
    PAID(5);

    val limit: Bandwidth
        get() = Bandwidth.classic(
            bucketCapacity.toLong(),
            Refill.intervally(bucketCapacity.toLong(), Duration.ofMinutes(1))
        )
    companion object {
        fun resolvePlanFromUser(hasPaid: Boolean): PricingPlan {
            return if (hasPaid) {
                PAID
            } else {
                FREE
            }
        }
    }
}
