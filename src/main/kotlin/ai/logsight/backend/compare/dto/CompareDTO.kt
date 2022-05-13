package ai.logsight.backend.compare.dto

import java.util.*

data class CompareDTO(
    val applicationId: UUID,
    val applicationName: String,
    val flushId: UUID?,
    val privateKey: String,
    val baselineTags: Map<String, String>,
    val compareTags: Map<String, String>
)
