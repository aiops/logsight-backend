package ai.logsight.backend.compare.ports.out

import java.net.http.HttpClient

interface HttpClientFactory {
    fun create(): HttpClient
}
