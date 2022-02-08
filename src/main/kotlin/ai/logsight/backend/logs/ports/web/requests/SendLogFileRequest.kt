package ai.logsight.backend.logs.ports.web.requests

import ai.logsight.backend.logs.domain.LogFormat
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class SendLogFileRequest(
    @get:NotEmpty(message = "applicationName must not be empty.")
    val applicationName: String,
    @get:NotEmpty(message = "file must not be empty")
    val file: MultipartFile,
    @get:NotEmpty(message = "tag must not be empty")
    val tag: String = "default",
    @get:Pattern(regexp = "UNKNOWN_FORMAT", message = "logFormat must either be in [UNKNOWN_FORMAT,]")
    @get:NotEmpty(message = "logFormat must not be empty.")
    val logFormat: LogFormat = LogFormat.UNKNOWN_FORMAT
)
