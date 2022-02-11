package ai.logsight.backend.application.ports.out.rpc.adapters.rest

import ai.logsight.backend.application.ports.out.rpc.RPCService
import ai.logsight.backend.application.ports.out.rpc.adapters.repsponse.RPCResponse
import ai.logsight.backend.application.ports.out.rpc.adapters.rest.config.AnalyticsManagerRESTConfigurationProperties
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
@Qualifier("REST")
class AnalyticsManagerREST(
    private val analyticsManagerConfig: AnalyticsManagerRESTConfigurationProperties
) : RPCService {

    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    override fun createApplication(createApplicationDTO: ApplicationDTO): RPCResponse? {
        val uri = this.getUriForApplicationEndpoint().build().toUri()
        val request =
            HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.ofString(createApplicationDTO.toString()))
                .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        // todo: Handle RPC Response
        return null
    }

    override fun deleteApplication(deleteApplicationDTO: ApplicationDTO): RPCResponse? {
        val uri = this.getUriForApplicationEndpoint().path(deleteApplicationDTO.id.toString()).build().toUri()
        val request = HttpRequest.newBuilder().uri(uri).DELETE().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        // todo: Handle RPC Response
        return null
    }

    private fun getUriForApplicationEndpoint() =
        UriComponentsBuilder.newInstance().scheme("https").host(analyticsManagerConfig.host)
            .port(analyticsManagerConfig.port).path("api").path(analyticsManagerConfig.apiVersion.toString())
            .path(analyticsManagerConfig.applicationsPath)
}
