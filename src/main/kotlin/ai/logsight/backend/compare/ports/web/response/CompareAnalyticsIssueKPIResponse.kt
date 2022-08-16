package ai.logsight.backend.compare.ports.web.response

data class CompareAnalyticsIssueKPIResponse(
    val listIssueKPIs: Map<Long, Long>
)
