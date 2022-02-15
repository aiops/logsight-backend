package ai.logsight.backend.verification.service

import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.verification.dto.VerificationDTO
import ai.logsight.backend.verification.out.rest.config.VerificationRESTConfigProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient

@Service
class ContinuousVerificationService(
    private val restConfigProperties: VerificationRESTConfigProperties
) {
    val resetTemplate = RestTemplateConnector()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val endpoint = buildVerificationEndpointURI()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    fun getVerificationData(verificationDTO: VerificationDTO): String {
//        val request =
//            HttpRequest.newBuilder()
//                .uri(endpoint)
//                .POST(HttpRequest.BodyPublishers.ofString(verificationDTO.toString()))
//                .build()
//        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
//            .toString()
        return resetTemplate.sendRequest(endpoint.toString(), query = mapper.writeValueAsString(verificationDTO))
    }

    private fun buildVerificationEndpointURI() =
        UriComponentsBuilder.newInstance()
            .scheme("http")
            .host(restConfigProperties.host)
            .port(restConfigProperties.port)
            .path("/api")
            .path("/v${restConfigProperties.apiVersion}")
            .path("/${restConfigProperties.endpoint}")
            .build()
            .toUri()
}
