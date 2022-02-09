package ai.logsight.backend.logs.domain.service.dto

import ai.logsight.backend.logs.domain.LogFormat
import org.springframework.web.multipart.MultipartFile
import java.util.*

data class LogFileDTO(
    val userEmail: String,
    val applicationId: UUID,
    val tag: String,
    val logFormat: LogFormat,
    val file: MultipartFile
)
