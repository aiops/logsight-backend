package ai.logsight.backend.logs.domain

import javax.validation.constraints.Pattern

data class LogsightLog(
    val id: String? = null,
    @get:Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(([+-]\\d{2}:\\d{2})|Z)?$",
        message = "timestamp must be defined as ISO 8601 " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    val timestamp: String,
    val message: String,
    @get:Pattern(
        regexp = "^INFO$|^WARNING$|^WARN$|^FINER$|^FINE$|^DEBUG$|^ERROR$|^ERR$|^EXCEPTION$|^SEVERE$",
        message = "level must be one of INFO|WARNING|WARN|FINE|FINER|DEBUG|ERR|ERROR|EXCEPTION|SEVERE"
    )
    val level: String,
    val tags: Map<String, String> = mapOf("default" to "default")
)
