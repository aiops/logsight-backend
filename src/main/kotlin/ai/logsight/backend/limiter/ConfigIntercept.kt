package ai.logsight.backend.limiter

import ai.logsight.backend.limiter.datalimit.DataLimitInterceptor
import ai.logsight.backend.limiter.verifications.VerificationLimitInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ConfigIntercept(
    private val dataInterceptor: DataLimitInterceptor,
    private val verificationLimitInterceptor: VerificationLimitInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(dataInterceptor)
            .addPathPatterns("/api/v1/logs")
            .addPathPatterns("/api/v1/logs/singles").order(Ordered.LOWEST_PRECEDENCE)
        registry.addInterceptor(verificationLimitInterceptor).order(Ordered.LOWEST_PRECEDENCE)
            .addPathPatterns("/api/v1/logs/compare")
    }
}
