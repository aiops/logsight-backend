package ai.logsight.backend.compare.controller.response

import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsDataPoint

data class CreateIncidentDataResponse(
    val data: List<IncidentResponse>
)
