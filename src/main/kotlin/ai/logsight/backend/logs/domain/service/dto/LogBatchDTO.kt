package ai.logsight.backend.logs.domain.service.dto

data class LogBatchDTO(
    val userKey: String,
    val applicationName: String,
    val logs: List<Log>
)
