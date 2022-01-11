package ai.logsight.backend.email.service.dto

import java.net.URL

data class EmailPropertiesDTO(
    val userEmail: String,
    val url: URL, // should have the format <baseUrl>?uuid=<UUID>&token=<userActivationToken>
)
