package ai.logsight.backend.results.ports.channel.adapter.zeromq.message

import org.springframework.http.HttpStatus
import java.util.*

data class ResultInitMessage(
    val id: UUID,
    val orderCounter: Long,
    val logsCount: Int,
    val currentLogsCount: Int,
    val description: String,
    val status: HttpStatus
)
