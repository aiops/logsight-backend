package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TableChartData(val hits: Hits)
