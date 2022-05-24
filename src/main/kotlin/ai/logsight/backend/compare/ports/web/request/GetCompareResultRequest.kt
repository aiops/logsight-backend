package ai.logsight.backend.compare.ports.web.request

import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class GetCompareResultRequest(
    @get:NotNull(message = "applicationId must not be null or empty.")
    val applicationId: UUID,
    @get:Pattern(
        regexp = "^[a-zA-Z0-9][a-zA-Z0-9_.-]+\$",
        message = "applicationName must follow the following regex pattern ^[a-zA-Z0-9][a-zA-Z0-9_.-]+\\\$."
    )
    val applicationName: String? = null,
    @RequestParam(required = false)
    val logsReceiptId: UUID?,
    @get:NotEmpty(message = "baselineTags must not be empty.")
    val baselineTags: Map<String, String> = mapOf("defaultTag" to "default"),
    @get:NotEmpty(message = "compareTags must not be empty.")
    val candidateTags: Map<String, String> = mapOf("defaultTag" to "default")
) {
    @AssertTrue(message = "One of applicationId or applicationName must not be empty")
    fun isValid(): Boolean {
        return Objects.nonNull(this.applicationName) || Objects.nonNull(this.applicationId)
    }
}
