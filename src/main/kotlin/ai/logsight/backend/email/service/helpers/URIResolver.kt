package ai.logsight.backend.email.service.helpers

import ai.logsight.backend.elasticsearch.config.CommonConfigurationProperties
import ai.logsight.backend.token.domain.Token
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URL

@Service
class URIResolver(val commonConfig: CommonConfigurationProperties) {
    fun getActivationURI(token: Token): URL {
        return URI(
            commonConfig.baseURL.scheme,
            commonConfig.baseURL.authority,
            commonConfig.baseURL.path,
            "uuid=${token.userId}&token=${token.token}",
            commonConfig.baseURL.fragment
        ).resolve("/api/v1/user/activate")
            .toURL() // TODO Can / should be this endpoint retrieved instead of hard-coded?
    }

    fun getPasswordResetURI(token: Token): URL {
        return URI(
            commonConfig.baseURL.scheme,
            commonConfig.baseURL.authority,
            commonConfig.baseURL.path,
            "uuid=${token.userId}&token=${token.token}",
            commonConfig.baseURL.fragment
        ).resolve("/api/v1/user/activate")
            .toURL() // TODO Can / should be this endpoint retrieved instead of hard-coded?
    }
}
