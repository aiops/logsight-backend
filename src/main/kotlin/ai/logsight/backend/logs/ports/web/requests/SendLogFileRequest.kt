package ai.logsight.backend.logs.ports.web.requests

import ai.logsight.backend.logs.domain.LogFormat
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SendLogFileRequest(
    @NotNull @NotEmpty
    val applicationName: String,
    @NotNull @NotEmpty
    val file: MultipartFile,
    @NotEmpty
    val tag: String = "default",
    val logFormat: LogFormat = LogFormat.UNKNOWN_FORMAT
)
