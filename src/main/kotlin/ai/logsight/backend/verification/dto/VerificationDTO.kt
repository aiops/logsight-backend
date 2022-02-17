package ai.logsight.backend.verification.dto

import java.util.*

data class VerificationDTO(
    val applicationId: UUID,
    val applicationName: String,
    val resultInitId: UUID?,
    val privateKey: String,
    val baselineTag: String,
    val compareTag: String
)
