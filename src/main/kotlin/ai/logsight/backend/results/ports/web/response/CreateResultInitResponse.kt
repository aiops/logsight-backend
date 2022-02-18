package ai.logsight.backend.results.ports.web.response

import ai.logsight.backend.results.domain.service.ResultInitStatus
import java.util.*

data class CreateResultInitResponse(
    val flushId: UUID,
    val status: ResultInitStatus
)
