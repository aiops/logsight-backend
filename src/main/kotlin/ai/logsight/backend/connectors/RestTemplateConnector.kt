package ai.logsight.backend.connectors

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

class RestTemplateConnector(val url: String, val username: String, val password: String) : Connector {
    fun sendRequest(query: String, headerName: String? = null): String {
        val request = ConnectorUtils.createHttpEntityHeader(query, headerName)
        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthentication(
            username, password
        ).build()
        return restTemplate.postForEntity<String>(url, request).body!!
    }
}

// create user
// create space
// create role
// create index (for app)
// set default index?
