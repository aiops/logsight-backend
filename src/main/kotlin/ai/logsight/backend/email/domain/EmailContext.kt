package ai.logsight.backend.email.domain

import ai.logsight.backend.token.domain.Token
data class EmailContext(
    var title: String = "",
    val userEmail: String,
    val token: Token,
)
