package ai.logsight.backend.autolog.ports.web.request

import java.util.UUID

data class AutoLogFeedbackRequest(
    val autoLogId: UUID,
    val isHelpful: Boolean
)
