package ai.logsight.backend.compare.ports.web.response

import ai.logsight.backend.charts.repository.entities.elasticsearch.VerticalBarBucket

data class CompareAnalyticsIssueKPIResponse(
    val listIssueKPIs: Map<Long, Long>
)
