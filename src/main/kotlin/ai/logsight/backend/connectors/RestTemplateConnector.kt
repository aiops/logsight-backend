package ai.logsight.backend.connectors

import ai.logsight.backend.common.dto.Credentials
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

open class RestTemplateConnector : Connector {
    fun sendRequest(url: String, credentials: Credentials, query: String, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthentication(
            credentials.username, credentials.password
        ).build()
        return restTemplate.postForEntity<String>(url, request).body!!
    }

    fun sendRequest(url: String, query: String, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().build()
        return restTemplate.postForEntity<String>(url, request).body!!
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
        println(url)
        return restTemplate.put(url, request)
    }
}
