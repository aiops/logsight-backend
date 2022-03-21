package ai.logsight.backend.results.ports.web.request

import java.util.*
import javax.validation.constraints.NotNull

data class CreateFlushRequest(
    @get:NotNull(message = "receiptId must not be empty")
    val receiptId: UUID
)
