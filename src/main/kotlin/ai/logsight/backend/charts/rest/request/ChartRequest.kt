package ai.logsight.backend.charts.rest.request

import ai.logsight.backend.charts.domain.dto.ChartConfig
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class ChartRequest(
    @NotEmpty(message = "chartConfig must not be empty.")
    val chartConfig: ChartConfig,
    @Pattern(
        regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
        message = "applicationId must be UUID type."
    )
    val applicationId: UUID?
)
