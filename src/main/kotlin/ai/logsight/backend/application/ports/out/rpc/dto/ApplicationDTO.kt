package ai.logsight.backend.application.ports.out.rpc.dto

import java.util.*

data class ApplicationDTO(
    val id: UUID,
    val name: String,
    val key: String,
    var action: String = "",
)
