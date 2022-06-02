package ai.logsight.backend.compare.domain.dto

import java.util.*

data class CompareDTO(
    val applicationId: UUID,
    val applicationName: String,
    val logsReceiptId: UUID?,
    val privateKey: String,
    val baselineTag: String,
    val compareTag: String
)
