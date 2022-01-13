package ai.logsight.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
@ConfigurationPropertiesScan
class LogsightBackendApplication

fun main(args: Array<String>) {
    runApplication<LogsightBackendApplication>(*args)
}
@Bean
fun encoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
}