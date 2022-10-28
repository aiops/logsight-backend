package ai.logsight.backend.autolog.ports.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AutoLogFeedbackResponse(
    val text: String = "Thank you!",
)
