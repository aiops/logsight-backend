package ai.logsight.backend.limiter.datalimit

import ai.logsight.backend.limiter.RateLimitInterceptor
import ai.logsight.backend.limiter.datalimit.service.DataPlanService
import ai.logsight.backend.users.domain.User
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class DataLimitInterceptor(
    private val dataPlanService: DataPlanService
) : RateLimitInterceptor() {
    override fun resolveBucketAndProbe(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: User
    ): ConsumptionProbe {
        val wrapped = ContentCachingRequestWrapper(request)
        val usedData = wrapped.contentLengthLong
        val tokenBucket: Bucket = dataPlanService.resolveBucket(user)
        return tokenBucket.tryConsumeAndReturnRemaining(usedData)
    }

}
