package ai.logsight.backend.compare.ports.web.request

import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class UpdateCompareStatusRequest(
    val compareId: String,
    @get:Min(1)
    @get:Max(3)
    val compareStatus: Long
)
