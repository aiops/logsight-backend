package ai.logsight.backend.application.ports.out.rpc.dto

import java.util.*

data class ApplicationResponse(
    val applicationId: UUID,
    val name: String
)
