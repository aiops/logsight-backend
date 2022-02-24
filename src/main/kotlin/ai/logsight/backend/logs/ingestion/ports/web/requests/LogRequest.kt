package ai.logsight.backend.logs.ingestion.ports.web.requests

data class LogRequest(
    val timestamp: String,
    val message: String,
    val level: String,
    val metadata: String,
)