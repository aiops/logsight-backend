package ai.logsight.backend.results.domain.service.command

import ai.logsight.backend.results.domain.service.ResultInitStatus
import java.util.*

data class UpdateResultInitStatusCommand(
    val id: UUID,
    val status: ResultInitStatus
)
