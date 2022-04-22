package ai.logsight.backend.logs.domain

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LogMessage(

    @get:Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(([+-]\\d{2}:\\d{2})|Z)?$",
        message = "timestamp must be defined as ISO 8601 " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    val timestamp: String?,
    val message: String,
    @get:Pattern(
        regexp = "^INFO$|^WARNING$|^WARN$|^FINER$|^FINE$|^DEBUG$|^ERROR$|^ERR$|^EXCEPTION$|^SEVERE$|^info$|^warning$|^warn$|^finer$|^fine$|^debug$|^error$|^err$|^exception$|^severe$",
        message = "level must be one of INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE or lowerletter variant of them."
    )
    val level: String? = null,
    val metadata: String? = null,
)
