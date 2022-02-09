package ai.logsight.backend.application.ports.out.rpc.adapters.repsponse

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.http.HttpStatus

data class RPCResponse(
    val id: String,
    val message: String,
    @JsonAlias("status") val statusCode: Int,
) {
    val status = HttpStatus.valueOf(statusCode)
}
