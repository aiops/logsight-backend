package ai.logsight.backend.charts.repository.entities.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HeatMapListBucket(
    val buckets: List<HeatMapResultBucket>
)
