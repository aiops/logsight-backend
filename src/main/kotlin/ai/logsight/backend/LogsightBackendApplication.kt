package ai.logsight.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class LogsightBackendApplication

fun main(args: Array<String>) {
    println("ApplicationStarted")
    runApplication<LogsightBackendApplication>(*args)
}
