package ai.logsight.backend.compare.ports.web.request

data class UpdateCompareStatusRequest(
    val compareId: String,
    val compareStatus: Long
)
