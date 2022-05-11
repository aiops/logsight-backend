package ai.logsight.backend.application.ports.out.rpc.dto

import java.util.*

data class ApplicationRPCResponse(
    val applicationId: UUID,
    val name: String,
    val displayName: String
)
