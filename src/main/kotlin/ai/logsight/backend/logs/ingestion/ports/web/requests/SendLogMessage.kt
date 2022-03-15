package ai.logsight.backend.logs.ingestion.ports.web.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SendLogMessage(

    @get:NotNull(message = "applicationId must be defined")
    val applicationId: UUID,
    @get:NotEmpty(message = "tag must not be empty")
    val tag: String = "default",

    @get:Pattern(
        regexp = "(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:Z]+)",
        message = "timestamp must be defined as ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "timestamp must not be null or empty")
    val timestamp: String,
    val message: String,
    @get:Pattern(
        regexp = "INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE",
        message = "level must be one of INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE"
    )
    val level: String? = null,
    val metadata: String? = null,
)
