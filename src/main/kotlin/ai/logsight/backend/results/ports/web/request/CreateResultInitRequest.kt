package ai.logsight.backend.results.ports.web.request

import ai.logsight.backend.results.domain.ResultOperations
import java.util.*
import javax.validation.constraints.NotNull

data class CreateResultInitRequest(
    @get:NotNull(message = "receiptId must not be empty")
    val receiptId: UUID
)
