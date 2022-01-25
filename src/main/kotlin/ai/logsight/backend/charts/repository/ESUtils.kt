package ai.logsight.backend.charts.repository

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class ESUtils {
    companion object {
        fun createElasticSearchRequestWithHeaders(timeJsonString: String): HttpEntity<String> {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val json = JSONObject(timeJsonString)
            return HttpEntity(json.toString(), headers)
        }

        fun createKibanaRequestWithHeaders(timeJsonString: String): HttpEntity<String> {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.add("kbn-xsrf", "true")
            val json = JSONObject(timeJsonString)
            return HttpEntity(json.toString(), headers)
        }
    }
}
