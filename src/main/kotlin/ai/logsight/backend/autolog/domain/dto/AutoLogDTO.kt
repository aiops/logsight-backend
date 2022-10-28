package ai.logsight.backend.autolog.domain.dto

import ai.logsight.backend.users.domain.User

data class AutoLogDTO(
    val user: User,
    val context: String,
    val fileName: String,
    val languageId: String,
    val source: String,
)
