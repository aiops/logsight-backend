package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.loxbear.logsight.charts.elasticsearch.HeatMapAggregations

@JsonIgnoreProperties(ignoreUnknown = true)
data class HeatMapData(val aggregations: HeatMapAggregations)
