package ai.logsight.backend.flush.ports.rpc.dto

import java.util.*

data class FlushDTO(
    val id: UUID,
    val orderNum: Long,
    val logsCount: Int,
    val operation: FlushDTOOperations
)

enum class FlushDTOOperations(private val operation: String) {
    FLUSH("FLUSH");

    override fun toString(): String {
        return operation
    }
}
