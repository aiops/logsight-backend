package ai.logsight.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@ConfigurationPropertiesScan
class LogsightBackendApplication

fun main(args: Array<String>) {
    println("ApplicationStarted")
    runApplication<LogsightBackendApplication>(*args)
}
