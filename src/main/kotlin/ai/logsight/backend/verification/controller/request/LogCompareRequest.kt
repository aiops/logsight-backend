package ai.logsight.backend.verification.controller.request

import java.util.*

data class LogCompareRequest(
    val applicationId: UUID,
    val baselineTag: String,
    val compareTag: String,
    val selectedTime: String
)
