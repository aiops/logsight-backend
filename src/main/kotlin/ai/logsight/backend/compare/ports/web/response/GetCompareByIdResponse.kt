package ai.logsight.backend.compare.ports.web.response

import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsCompareDataPoint

data class GetCompareByIdResponse(
    val listCompare: List<HitsCompareDataPoint>
)
