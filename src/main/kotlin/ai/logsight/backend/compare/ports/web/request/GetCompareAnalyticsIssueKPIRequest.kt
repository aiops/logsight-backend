package ai.logsight.backend.compare.ports.web.request

import javax.validation.constraints.NotEmpty

data class GetCompareAnalyticsIssueKPIRequest(
    @get:NotEmpty(message = "Baseline tags must not be empty.")
    val baselineTags: Map<String, String> = mapOf("defaultTag" to "default"),
)
