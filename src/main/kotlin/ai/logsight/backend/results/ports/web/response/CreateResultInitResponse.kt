package ai.logsight.backend.results.ports.web.response

import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.results.domain.service.ResultInitStatus
import java.util.*

data class CreateResultInitResponse(
    val id: UUID,
    val status: ResultInitStatus
)
