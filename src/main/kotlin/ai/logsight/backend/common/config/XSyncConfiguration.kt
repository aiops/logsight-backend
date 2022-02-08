package ai.logsight.backend.common.config

import ai.logsight.backend.application.domain.Application
import com.antkorwin.xsync.XSync
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class XSyncConfiguration {
    @Bean
    fun xSync(): XSync<Application> {
        return XSync<Application>()
    }
}
