package ai.logsight.backend.logs.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.constraints.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LogMessage(

    @get:Pattern(
        regexp = "(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "timestamp must be defined as ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    val timestamp: String?,
    val message: String,
    @get:Pattern(
        regexp = "INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE",
        message = "level must be one of INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE"
    )
    val level: String? = null,
    val metadata: String? = null,
)
