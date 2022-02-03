package ai.logsight.backend.connectors.rest

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class ConnectorUtils {
    companion object {

        fun createHttpEntityHeader(jsonRequest: String, headerName: String?): HttpEntity<String> {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            if (headerName != null) {
                headers.add(headerName, "true")
            }

            return HttpEntity(JSONObject(jsonRequest).toString(), headers)
        }
    }
}
