package ai.logsight.backend.verification.dto

data class VerificationDTO(
    val applicationName: String,
    val privateKey: String,
    val baselineTag: String,
    val compareTag: String
)
