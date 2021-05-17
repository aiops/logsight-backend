package utils

import javafx.application.Application
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths

class UtilsService {
    companion object {
        fun readFileAsString(path: String): String {
            return String(Files.readAllBytes(Paths.get(path)))
        }

        fun createElasticSearchRequestWithHeaders(timeJsonString: String): HttpEntity<String> {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val json = JSONObject(timeJsonString)
            return HttpEntity(json.toString(), headers)
        }

        //TODO should be improved
        fun getLeadingNumber(param: String): String = param.trim().takeWhile { c -> c.isDigit() || c == '.' }

        fun getApplicationIdFromIndex(applications: Map<String, Long>, index: String): Long {
            return applications[index.split("_").subList(1, index.split("_").size - 1).joinToString("_")] ?: -1L
        }

    }
}