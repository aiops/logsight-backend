package ai.logsight.backend.results.ports.rpc.dto

import java.util.*

data class FlushDTO(
    val id: UUID,
    val orderNum: Long,
    val logsCount: Int,
    val operation: FlushDTOOperations
)

enum class FlushDTOOperations(val operation: String) {
    FLUSH("FLUSH");

    override fun toString(): String {
        return operation
    }
}
