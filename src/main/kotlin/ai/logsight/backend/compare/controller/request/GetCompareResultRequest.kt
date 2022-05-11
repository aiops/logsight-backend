package ai.logsight.backend.compare.controller.request

import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class GetCompareResultRequest(
    @get:NotNull(message = "applicationId must not be null or empty.")
    val applicationId: UUID,
    @RequestParam(required = false)
    val flushId: UUID?,
    @get:NotEmpty(message = "baselineTag must not be empty.")
    val baselineTag: String,
    @get:NotEmpty(message = "compareTag must not be empty.")
    val candidateTag: String
)
