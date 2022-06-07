package ai.logsight.backend.logs.ingestion.ports.web.requests

import ai.logsight.backend.logs.domain.enums.LogFormats
import org.springframework.web.multipart.MultipartFile
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class SendLogFileRequest(
    @get:NotNull(message = "file must not be empty")
    val file: MultipartFile,
    @get:NotEmpty(message = "tag must not be empty")
    val tag: String = "default",
    @get:NotNull(message = "logFormat must not be empty.")
    val logFormats: LogFormats = LogFormats.UNKNOWN_FORMAT
)
