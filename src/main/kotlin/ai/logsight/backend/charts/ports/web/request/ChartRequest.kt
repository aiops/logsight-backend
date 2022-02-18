package ai.logsight.backend.charts.ports.web.request

import ai.logsight.backend.charts.domain.dto.ChartConfig
import java.util.*
import javax.validation.constraints.NotNull

data class ChartRequest(
    @get:NotNull(message = "chartConfig must not be empty.")
    val chartConfig: ChartConfig,
    val applicationId: UUID?
)