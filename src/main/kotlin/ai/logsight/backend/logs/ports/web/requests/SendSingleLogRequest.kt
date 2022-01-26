package ai.logsight.backend.logs.ports.web.requests

import java.util.*

data class SendSingleLogRequest(
    val appId: UUID,
    val userId: UUID,
    val log: String
)