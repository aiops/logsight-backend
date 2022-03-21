package ai.logsight.backend.flush.domain.service.command

import ai.logsight.backend.flush.domain.service.FlushStatus
import java.util.*

data class UpdateFlushStatusCommand(
    val id: UUID,
    val status: FlushStatus
)
