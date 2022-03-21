package ai.logsight.backend.flush.ports.web.response

import ai.logsight.backend.flush.domain.service.FlushStatus
import java.util.*

data class FlushResponse(
    val flushId: UUID,
    val status: FlushStatus
)
