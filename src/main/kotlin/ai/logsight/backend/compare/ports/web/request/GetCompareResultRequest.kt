package ai.logsight.backend.compare.ports.web.request

import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.validation.constraints.NotEmpty

data class GetCompareResultRequest(
    @RequestParam(required = false)
    val logReceiptId: UUID? = null,
    @get:NotEmpty(message = "Baseline tags must not be empty.")
    val baselineTags: Map<String, String>,
    @get:NotEmpty(message = "Candidate tags must not be empty.")
    val candidateTags: Map<String, String>
)
