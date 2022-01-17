package ai.logsight.backend.user.persistence

import ai.logsight.backend.token.service.TokenServiceImpl
import ai.logsight.backend.user.adapters.persistence.UserStorageImpl
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class UserStoreServiceConfiguration {
    @Bean
    @Primary
    fun userStoreService(): UserStorageImpl {
        return Mockito.mock(UserStorageImpl::class.java)
    }
}
