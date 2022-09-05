package ai.logsight.backend.limiter.datalimit.service

import ai.logsight.backend.users.domain.User
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class DataPlanService {
    private val cache: MutableMap<User, Bucket> = ConcurrentHashMap()
    fun resolveBucket(user: User): Bucket {
        return cache.computeIfAbsent(
            user
        ) {
            newBucket(user)
        }
    }

    private fun newBucket(user: User): Bucket {
        val pricingPlan: DataPlan = DataPlan.resolveDataPlanFromUser(user.userCategory)
        return bucket(pricingPlan.limit)
    }

    private fun bucket(limit: Bandwidth) = Bucket.builder().addLimit(limit).build()
}
