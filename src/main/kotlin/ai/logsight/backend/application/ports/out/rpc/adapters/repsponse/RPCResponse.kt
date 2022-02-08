package ai.logsight.backend.application.ports.out.rpc.adapters.repsponse

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.http.HttpStatus
import java.util.*

data class RPCResponse(
    val timestamp: Date,
    val message: String,
    @JsonAlias("status") val statusCode: Int,
) {
    val status = HttpStatus.valueOf(statusCode)
}
