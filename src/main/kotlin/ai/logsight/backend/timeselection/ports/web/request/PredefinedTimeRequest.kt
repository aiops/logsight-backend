package ai.logsight.backend.timeselection.ports.web.request

import ai.logsight.backend.timeselection.ports.out.persistence.DateTimeType
import javax.validation.constraints.*

data class PredefinedTimeRequest(
    @get:NotEmpty(message = "name must not be empty.")
    val name: String,
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "starTime must be defined relative time in minutes (e.g., now, now-22m) or ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "startTime must not be empty.")
    val startTime: String,
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "starTime must be defined relative time in minutes (e.g., now, now-22m) or ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "endTime must not be empty.")
    val endTime: String,
    @get:NotNull(message = "dateTimeType must not be empty.")
    val dateTimeType: DateTimeType
)
