package ai.logsight.backend.logs.ports.out.stream.dto

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.util.*

@JsonSerialize
data class LogDTO(
    val userId: UUID,
    val appId: UUID,
    val logType: String = "Unknown",
    val log: String
)