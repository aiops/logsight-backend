package ai.logsight.backend.incidents.controller.request

import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class GetIncidentResultRequest(
    @get:NotNull(message = "applicationId must not be null or empty.")
    val applicationId: UUID,
    @RequestParam(required = false)
    val flushId: UUID?,
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "startTime must be defined as ISO 8601 timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "startTime must not be empty.")
    val startTime: String,
    @get:Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "stopTime must be defined as ISO 8601 timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @get:NotEmpty(message = "stopTime must not be empty.")
    val stopTime: String,
)
