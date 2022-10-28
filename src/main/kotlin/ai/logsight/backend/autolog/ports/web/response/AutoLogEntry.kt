package ai.logsight.backend.autolog.ports.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AutoLogEntry(
    val position: String = "above",
    val logMessage: String = "# insert log message",
)
