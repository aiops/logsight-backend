package ai.logsight.backend.logwriter.ports.web.request

import java.util.UUID

data class LogWriterFeedbackRequest(
    val autoLogId: UUID,
    val isHelpful: Boolean
)
