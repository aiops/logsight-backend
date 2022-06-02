package ai.logsight.backend.charts.domain.dto

import org.springframework.beans.factory.annotation.Value
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class ChartConfig(
    var parameters: MutableMap<String, Any>,
)
