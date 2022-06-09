package ai.logsight.backend.compare.domain.dto

import java.util.*

data class CompareDTO(
    val logReceiptId: UUID?,
    val privateKey: String,
    val baselineTags: Map<String, String>,
    val candidateTags: Map<String, String>
)
