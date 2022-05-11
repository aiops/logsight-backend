package ai.logsight.backend.email.domain

import ai.logsight.backend.email.domain.service.helpers.EmailTemplateTypes
import ai.logsight.backend.token.domain.Token

data class EmailContext(
    val title: String = "",
    val userEmail: String,
    val token: Token,
    val template: EmailTemplateTypes
)
