package ai.logsight.backend.results.ports.web.request

import java.util.*
import javax.validation.constraints.NotNull

data class CreateCheckStatusResultInitRequest(
    @get:NotNull(message = "receiptId must not be empty")
    val flushId: UUID
)
