package ai.logsight.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LogsightBackendApplication

fun main(args: Array<String>) {
    runApplication<LogsightBackendApplication>(*args)
}
