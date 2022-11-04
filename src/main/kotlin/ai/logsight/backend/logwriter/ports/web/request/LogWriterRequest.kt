package ai.logsight.backend.logwriter.ports.web.request

import javax.validation.constraints.NotEmpty

data class LogWriterRequest(
    @get:NotEmpty(message = "languageId must not be empty.")
    val language: String,
    @get:NotEmpty(message = "fileName must not be empty.")
    val fileName: String,
    @get:NotEmpty(message = "source must not be empty.")
    val source: String,
    @get:NotEmpty(message = "context must not be empty.")
    val code: String,
)
