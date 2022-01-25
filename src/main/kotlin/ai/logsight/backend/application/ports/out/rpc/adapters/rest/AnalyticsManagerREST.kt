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
        val baseURI = this.createBase()
        val uri = baseURI.path(analyticsManagerConfig.createPath)
            .queryParam("app_name", createApplicationDTO.name)
            .queryParam("app_id", createApplicationDTO.id).build().toUri()

        val request = HttpRequest.newBuilder().uri(uri).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO) {
        val baseURI = this.createBase()
        val uri = baseURI.path(analyticsManagerConfig.deletePath)
            .queryParam("app_id", deleteApplicationDTO.id).build().toUri()

        val request = HttpRequest.newBuilder().uri(uri).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun createBase() = UriComponentsBuilder.newInstance()
        .scheme("https")
        .host(analyticsManagerConfig.host)
        .port(analyticsManagerConfig.port)
        .path("api")
        .path(analyticsManagerConfig.apiVersion.toString())
}