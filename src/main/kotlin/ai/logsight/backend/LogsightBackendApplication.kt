package ai.logsight.backend

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.common.logging.LoggerImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class LogsightBackendApplication

fun main(args: Array<String>) {
    val logger = LoggerImpl(LogsightBackendApplication::class.java)
    runApplication<LogsightBackendApplication>(*args)
    logger.info("Application ${LogsightBackendApplication::class.toString()} started.")
}
