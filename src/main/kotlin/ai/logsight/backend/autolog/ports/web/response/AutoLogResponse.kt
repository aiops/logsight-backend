package ai.logsight.backend.autolog.ports.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class AutoLogResponse(
    val listAutoLogs: List<AutoLogEntry> = listOf(),
    val autoLogId: UUID,
    val shouldShowFeedback: Boolean
)
