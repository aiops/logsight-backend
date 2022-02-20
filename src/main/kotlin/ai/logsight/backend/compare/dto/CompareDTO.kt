package ai.logsight.backend.compare.dto

import java.util.*

data class CompareDTO(
    val applicationId: UUID,
    val applicationName: String,
    val resultInitId: UUID?,
    val privateKey: String,
    val baselineTag: String,
    val compareTag: String
)
