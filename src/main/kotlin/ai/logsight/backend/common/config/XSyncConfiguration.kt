package ai.logsight.backend.common.config

import com.antkorwin.xsync.XSync
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class XSyncConfiguration {
    @Bean
    fun xSync(): XSync<String> {
        return XSync<String>()
    }
}
