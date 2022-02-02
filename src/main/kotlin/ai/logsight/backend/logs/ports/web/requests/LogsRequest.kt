package ai.logsight.backend.logs.ports.web.requests

import java.util.*

data class LogsRequest(
    val appId: UUID,
    val logs: List<String>
)