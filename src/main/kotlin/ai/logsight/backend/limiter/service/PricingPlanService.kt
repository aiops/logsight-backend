package ai.logsight.backend.limiter.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PricingPlanService {
    private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()
    fun resolveBucket(userKey: String, hasPaid: Boolean): Bucket {
        return cache.computeIfAbsent(
            userKey
        ) {
            newBucket(hasPaid)
        }
    }

    private fun newBucket(hasPaid: Boolean): Bucket {
        val pricingPlan: PricingPlan = PricingPlan.resolvePlanFromUser(hasPaid)
        return bucket(pricingPlan.limit)
    }

    private fun bucket(limit: Bandwidth) = Bucket.builder().addLimit(limit).build()
}
