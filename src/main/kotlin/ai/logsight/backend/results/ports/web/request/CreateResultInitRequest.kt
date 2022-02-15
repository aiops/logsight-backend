package ai.logsight.backend.results.ports.web.request

import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class CreateResultInitRequest(
    @get:NotNull(message = "receiptId must not be empty")
    val receiptId: UUID
)
