package ai.logsight.backend.autolog.domain.dto

import ai.logsight.backend.users.domain.User

data class AutoLogDTO(
    val user: User,
    val code: String,
    val language: String
)
