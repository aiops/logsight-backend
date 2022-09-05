package ai.logsight.backend.limiter.verifications.service

import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.UserCategory
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class VerificationPlanService(
    private val verificationLimitConfigProperties: VerificationLimitConfigProperties
) {
    private val cache: MutableMap<User, Bucket> = ConcurrentHashMap()
    fun resolveBucket(user: User): Bucket {
        return cache.computeIfAbsent(
            user
        ) {
            newBucket(user)
        }
    }

    private fun newBucket(user: User): Bucket {
        val pricingPlan: VerificationPlan = resolveVerificationPlanFromUser(user.userCategory)
        return bucket(pricingPlan.limit)
    }
    fun resolveVerificationPlanFromUser(userType: UserCategory): VerificationPlan =
        when (userType) {
            UserCategory.FREEMIUM -> VerificationPlan(verificationLimitConfigProperties.freemium)
            UserCategory.CORPORATE -> VerificationPlan(verificationLimitConfigProperties.corporate)
            UserCategory.DEVELOPER -> VerificationPlan(verificationLimitConfigProperties.developer)
        }
    private fun bucket(limit: Bandwidth) = Bucket.builder().addLimit(limit).build()
}
