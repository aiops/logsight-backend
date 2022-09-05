package ai.logsight.backend.limiter.verifications

import ai.logsight.backend.limiter.RateLimitInterceptor
import ai.logsight.backend.limiter.verifications.service.VerificationPlanService
import ai.logsight.backend.users.domain.User
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe

import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class VerificationLimitInterceptor(
    private val verificationPlanService: VerificationPlanService
) : RateLimitInterceptor() {
    override fun resolveBucketAndProbe(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: User
    ): ConsumptionProbe {
        val tokenBucket: Bucket = verificationPlanService.resolveBucket(user)
        return tokenBucket.tryConsumeAndReturnRemaining(1)
    }
}
