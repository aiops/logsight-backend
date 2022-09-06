package ai.logsight.backend.limiter

import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.FindUserServiceImpl
import io.github.bucket4j.ConsumptionProbe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.time.Duration.Companion.seconds

@Component
abstract class RateLimitInterceptor : HandlerInterceptor {

    @Autowired
    protected lateinit var userDetailsService: FindUserServiceImpl

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val email = SecurityContextHolder.getContext().authentication.principal.toString()
        val user = userDetailsService.findUserByEmail(email)
        val probe = resolveBucketAndProbe(request, response, user)
        return actionOnConsume(probe, response)
    }

    fun actionOnConsume(probe: ConsumptionProbe, response: HttpServletResponse): Boolean {
        return if (probe.isConsumed) {
            response.addHeader(HEADER_LIMIT_REMAINING, probe.remainingTokens.toString())
            true
        } else {
            val waitForRefill = probe.nanosToWaitForRefill / 1000000000
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.addHeader(HEADER_RETRY_AFTER, waitForRefill.toString())
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You have exhausted your API Request Quota. Please retry after ${waitForRefill.seconds}, or upgrade your plan.") // 429
            false
        }
    }
    abstract fun resolveBucketAndProbe(request: HttpServletRequest, response: HttpServletResponse, user: User): ConsumptionProbe

    companion object {
        private const val HEADER_API_KEY = "X-api-key"
        private const val HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining"
        private const val HEADER_RETRY_AFTER = "X-Rate-Limit-Retry-After-Seconds"
    }
}
