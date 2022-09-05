package ai.logsight.backend.limiter.verifications.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

class VerificationPlan(private val bucketCapacity: Long) {

    val limit: Bandwidth
        get() = Bandwidth.classic(
            bucketCapacity,
            Refill.intervally(bucketCapacity, Duration.ofMinutes(1))
        )
}
