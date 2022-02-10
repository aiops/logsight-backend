package ai.logsight.backend.logs.domain.service.dto

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.logs.domain.LogFormats
import ai.logsight.backend.users.domain.User
import org.springframework.web.multipart.MultipartFile

data class LogFileDTO(
    val user: User,
    val application: Application,
    val tag: String,
    val logFormats: LogFormats,
    val file: MultipartFile
)
