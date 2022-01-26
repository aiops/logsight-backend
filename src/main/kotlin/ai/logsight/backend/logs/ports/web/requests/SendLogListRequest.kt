package ai.logsight.backend.logs.ports.web.requests

import java.util.*

data class SendLogListRequest(
    val appId: UUID,
    val userId: UUID,
    val log: List<String>
)