package ai.logsight.backend

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.limiter.RateLimitInterceptor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.servlet.config.annotation.InterceptorRegistry

@SpringBootApplication
@ConfigurationPropertiesScan
class LogsightBackendApplication
fun main(args: Array<String>) {
    val logger = LoggerImpl(LogsightBackendApplication::class.java)

    runApplication<LogsightBackendApplication>(*args)
    logger.info("Application ${LogsightBackendApplication::class} started.")
}
