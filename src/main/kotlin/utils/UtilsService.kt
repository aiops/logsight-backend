package utils

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class UtilsService {

    companion object {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        fun readFileAsString(path: String): String {
            return String(Files.readAllBytes(Paths.get(path)))
        }

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

        //TODO should be improved
        fun getLeadingNumber(param: String): String = param.trim().takeWhile { c -> c.isDigit() || c == '.' }

        fun getApplicationIdFromIndex(applications: Map<String, Long>, index: String): Long {
            return applications[index.split("_").subList(1, index.split("_").size - 1).joinToString("_")] ?: -1L
        }

        fun getTimeIntervalAggregate(startTimeString: String, endTimeString: String, numberOfPoints: Int = 5): String =
            if (startTimeString.contains("now")) {
                val minutes = startTimeString.replace("[^0-9]".toRegex(), "").toInt()
                if (minutes >= numberOfPoints) {
                    "${minutes / numberOfPoints}m"
                } else {
                    "10s"
                }
            } else {
                val startDate = try {
                    ZonedDateTime.parse(startTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } catch (e: Exception) {
                    LocalDateTime.parse(startTimeString, formatter)
                }
                val endDate = try {
                    ZonedDateTime.parse(endTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } catch (e: Exception) {
                    LocalDateTime.parse(endTimeString, formatter)
                }
                val differenceMinutes = ChronoUnit.MINUTES.between(startDate, endDate)
                if (differenceMinutes >= numberOfPoints) {
                    "${differenceMinutes / numberOfPoints}m"
                } else {
                    "10s"
                }
            }
    }
}