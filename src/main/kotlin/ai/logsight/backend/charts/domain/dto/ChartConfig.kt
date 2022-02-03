package ai.logsight.backend.charts.domain.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class ChartConfig(
    @Pattern(
        regexp = "heatmap|barchart|piechart|tablechart",
        message = "type must be one of ['heatmap', 'barchart', 'piechart', 'tablechart']."
    )
    @NotEmpty(message = "type must not be empty.")
    var type: String,
    @Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "starTime must be defined relative time in minutes (e.g., now, now-22m) or ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @NotEmpty(message = "startTime must not be empty.")
    val startTime: String,
    @Pattern(
        regexp = "now-\\d+m|now|(\\d{4}-\\d{2}-\\d{2}[A-Z]+\\d{2}:\\d{2}:\\d{2}.[0-9+-:]+)",
        message = "starTime must be defined relative time in minutes (e.g., now, now-22m) or ISO timestamp " +
            "YYYY-MM-DDTHH:mm:ss.SSSSSS+HH:00. If timezone is not specified, UTC is default."
    )
    @NotEmpty(message = "stopTime must not be empty.")
    val stopTime: String,
    @Pattern(
        regexp = "system_overview|incidents|verification",
        message = "feature must be one of ['system_overview', 'incidents', 'verification']."
    )
    @NotEmpty(message = "feature must not be empty.")
    val feature: String,
    @Pattern(
        regexp = "log_ad|log_agg|incidents",
        message = "indexType must be one of ['log_ad', 'log_agg', 'incidents']."
    )
    @NotEmpty(message = "indexType must not be empty.")
    val indexType: String
)
