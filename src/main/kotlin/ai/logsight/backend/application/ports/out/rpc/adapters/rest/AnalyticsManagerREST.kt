package ai.logsight.backend.application.ports.out.rpc.adapters.rest

import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.rpc.adapters.rest.config.AnalyticsManagerRESTConfigurationProperties
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class AnalyticsManagerREST(
    private val analyticsManagerConfig: AnalyticsManagerRESTConfigurationProperties
) : AnalyticsManagerRPC {

    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    override fun createApplication(createApplicationDTO: ApplicationDTO) {
        val uri = this.getUriForApplicationEndpoint().build().toUri()
        val request =
            HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.ofString(createApplicationDTO.toString()))
                .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO) {
        val uri = this.getUriForApplicationEndpoint().path(deleteApplicationDTO.id.toString()).build().toUri()
        val request = HttpRequest.newBuilder().uri(uri).DELETE().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun getUriForApplicationEndpoint() = UriComponentsBuilder.newInstance()
        .scheme("https")
        .host(analyticsManagerConfig.host)
        .port(analyticsManagerConfig.port)
        .path("api")
        .path(analyticsManagerConfig.apiVersion.toString())
        .path(analyticsManagerConfig.applicationsPath)
}
