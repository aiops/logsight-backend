package ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ESHitsIncidents(
    val hits: List<ESHitsIncident>
)
