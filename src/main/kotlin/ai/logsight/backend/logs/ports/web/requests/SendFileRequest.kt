package ai.logsight.backend.logs.ports.web.requests

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class SendFileRequest(
    val applicationName: String, // TODO("Needs to be applicationId")
    val file: MultipartFile,
    @NotNull
    val tag: String = "default"
)
