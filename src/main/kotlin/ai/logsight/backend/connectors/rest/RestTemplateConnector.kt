package ai.logsight.backend.connectors.rest

import ai.logsight.backend.common.dto.Credentials
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity

@Component
class RestTemplateConnector {
    fun sendRequest(
        url: String,
        credentials: Credentials,
        query: String,
        headerName: String? = null
    ): ResponseEntity<String> {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthentication(
            credentials.username, credentials.password
        ).build()
        return restTemplate.postForEntity(url, request)
    }

    fun sendRequest(url: String, query: String, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().build()
        return restTemplate.postForEntity<String>(url, request).body!!
    }

    fun getRequest(url: String, query: String, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().build()
        return restTemplate.getForEntity<String>(url, request).body!!
    }

    fun putRequest(url: String, credentials: Credentials, query: String, headerName: String? = null) {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthentication(
            credentials.username, credentials.password
        ).build()
        return restTemplate.put(url, request)
    }

    fun putRequest(url: String, query: String, headerName: String? = null) {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().build()
        return restTemplate.put(url, request)
    }

    fun deleteRequest(url: String, credentials: Credentials, query: String?, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthentication(
            credentials.username, credentials.password
        ).build()
        return restTemplate.exchange(url, HttpMethod.DELETE, request, String::class.java).toString()
    }

    fun deleteRequest(url: String, query: String?, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().build()
        return restTemplate.exchange(url, HttpMethod.DELETE, request, String::class.java).toString()
    }
}
