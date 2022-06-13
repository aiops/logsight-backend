package ai.logsight.backend.charts.repository.entities.elasticsearch.incidents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ESIncidents(
    val hits: ESHitsIncidents
)
