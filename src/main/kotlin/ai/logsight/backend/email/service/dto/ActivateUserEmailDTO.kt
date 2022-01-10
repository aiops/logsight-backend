package ai.logsight.backend.email.service.dto

import java.net.URL

data class ActivateUserEmailDTO(
    val userEmail: String,
    val activationUrl: URL, // should have the format <baseUrl>?uuid=<UUID>&token=<userActivationToken>
)
