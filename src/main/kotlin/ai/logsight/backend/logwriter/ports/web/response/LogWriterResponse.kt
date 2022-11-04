package ai.logsight.backend.logwriter.ports.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogWriterResponse(
    val listWriteLogs: List<LogWriterEntry> = listOf(),
    val logWriterId: UUID,
    val shouldShowFeedback: Boolean
)
