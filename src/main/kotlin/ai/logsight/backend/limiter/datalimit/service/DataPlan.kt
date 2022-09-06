package ai.logsight.backend.limiter.datalimit.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import java.time.Duration

class DataPlan(private val bucketCapacity: Long) {
    val limit: Bandwidth
        get() = Bandwidth.classic(
            bucketCapacity,
            Refill.intervally(bucketCapacity, Duration.ofDays(30))
        )
}