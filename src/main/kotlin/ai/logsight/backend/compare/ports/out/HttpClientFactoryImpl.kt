package ai.logsight.backend.compare.ports.out

import org.springframework.stereotype.Component
import java.net.http.HttpClient

@Component
class HttpClientFactoryImpl : HttpClientFactory {
    override fun create(): HttpClient = HttpClient.newBuilder().build()
}
