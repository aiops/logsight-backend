package ai.logsight.backend.flush.ports.channel.adapter.zeromq.message

import org.springframework.http.HttpStatus
import java.util.*

data class FlushMessage(
    val id: UUID,
    val orderNum: Long,
    val logsCount: Int,
    val currentLogsCount: Int,
    val description: String,
    val status: HttpStatus
)
