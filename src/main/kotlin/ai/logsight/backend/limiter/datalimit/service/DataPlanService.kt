package ai.logsight.backend.limiter.datalimit.service

import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.UserCategory
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import reactor.util.function.Tuple2
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.persistence.Tuple

@Service
class DataPlanService(
    private val dataPlanConfigProperties: DataPlanConfigProperties
) {
    private val cache: MutableMap<Pair<UUID, UserCategory>, Bucket> = ConcurrentHashMap()
    fun resolveBucket(user: User): Bucket {
        return cache.computeIfAbsent(
            Pair(user.id,user.userCategory)
        ) {
            newBucket(user)
        }
    }

    fun resolveDataPlanFromUser(userType: UserCategory): DataPlan =
        when (userType) {
            UserCategory.FREEMIUM -> DataPlan(dataPlanConfigProperties.freemium)
            UserCategory.CORPORATE -> DataPlan(dataPlanConfigProperties.corporate)
            UserCategory.DEVELOPER -> DataPlan(dataPlanConfigProperties.developer)
        }

    private fun newBucket(user: User): Bucket {
        val pricingPlan: DataPlan = resolveDataPlanFromUser(user.userCategory)
        return bucket(pricingPlan.limit)
    }

    private fun bucket(limit: Bandwidth) = Bucket.builder().addLimit(limit).build()
}
