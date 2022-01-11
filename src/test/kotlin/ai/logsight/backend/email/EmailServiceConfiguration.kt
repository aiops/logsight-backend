package ai.logsight.backend.email

import ai.logsight.backend.email.service.EmailService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("test")
@Configuration
class EmailServiceConfiguration {
    @Bean
    @Primary
    fun emailService(): EmailService {
        return Mockito.mock(EmailService::class.java)
    }
}
