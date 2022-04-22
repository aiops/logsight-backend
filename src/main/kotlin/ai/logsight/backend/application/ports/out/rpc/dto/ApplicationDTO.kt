package ai.logsight.backend.application.ports.out.rpc.dto

import java.util.*

data class ApplicationDTO(
    val id: UUID,
    val name: String,
    val applicationKey: String,
    val userKey: String,
    var action: ApplicationDTOActions? = null,
)

enum class ApplicationDTOActions(val action: String) {
    CREATE("CREATE"),
    DELETE("DELETE");

    override fun toString(): String {
        return action
    }
}
