package ai.logsight.backend.charts.rest.request

import ai.logsight.backend.charts.domain.dto.ChartConfig
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class ChartRequest(
    @get:NotNull(message = "chartConfig must not be empty.")
    val chartConfig: ChartConfig,
    val applicationId: UUID?
)