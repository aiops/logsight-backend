package ai.logsight.backend.logwriter.domain.dto

import ai.logsight.backend.users.domain.User

data class LogWriterDTO(
    val user: User,
    val code: String,
    val language: String
)
