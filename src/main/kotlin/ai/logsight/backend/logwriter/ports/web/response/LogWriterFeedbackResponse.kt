package ai.logsight.backend.logwriter.ports.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogWriterFeedbackResponse(
    val text: String = "Thank you!",
)
