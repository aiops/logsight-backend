package ai.logsight.backend.logs.ingestion.ports.web.requests

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LogRequest(
    val timestamp: String?,
    val message: String,
    val level: String?,
    val metadata: String?,
)
