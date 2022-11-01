package ai.logsight.backend.autolog.ports.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AutoLogEntry(
    @JsonProperty("start_line_number")
    val startLineNumber: Long,
    @JsonProperty("start_col_number")
    val startColNumber: Long,
    @JsonProperty("end_line_number")
    val endLineNumber: Long,
    @JsonProperty("end_col_number")
    val endColNumber: Long,
    @JsonProperty("log_message")
    val logMessage: String,
)
