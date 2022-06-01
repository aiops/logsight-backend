package ai.logsight.backend.compare.ports.web.response

import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsCompareAllDataPoint

data class GetAllCompareResponse(
    val listCompare: List<HitsCompareAllDataPoint>
)
