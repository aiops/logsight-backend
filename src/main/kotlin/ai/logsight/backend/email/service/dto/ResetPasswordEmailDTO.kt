package ai.logsight.backend.email.service.dto

import java.net.URL

data class ResetPasswordEmailDTO(
    val userEmail: String,
    val passwordResetUrl: URL,
)
