package ai.logsight.backend.application.ports.out.rpc.dto

import java.util.*

data class ApplicationDTO(
    val id: UUID,
    val name: String,
    val index: String,
    val userKey: String,
    val action: ApplicationDTOActions,
)

enum class ApplicationDTOActions(val action: String) {
    CREATE("CREATE"),
    DELETE("DELETE");

    override fun toString(): String {
        return action
    }
}
