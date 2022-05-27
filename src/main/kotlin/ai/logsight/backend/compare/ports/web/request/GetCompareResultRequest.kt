package ai.logsight.backend.compare.ports.web.request

import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class GetCompareResultRequest(
    @RequestParam(required = false)
    val logsReceiptId: UUID? = null,
    @get:NotEmpty(message = "Baseline tags must not be empty.")
    val baselineTags: Map<String, String> = mapOf("defaultTag" to "default"),
    @get:NotEmpty(message = "Candidate tags must not be empty.")
    val candidateTags: Map<String, String> = mapOf("defaultTag" to "default")
)
