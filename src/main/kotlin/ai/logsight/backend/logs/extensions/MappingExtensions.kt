package ai.logsight.backend.logs.extensions

import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogEvent
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage

fun SendLogMessage.toLogsightLog() = LogsightLog(
    id = this.applicationId.toString(),
    event = LogEvent(timestamp = this.timestamp, message = this.message, level = this.level?.uppercase() ?: "INFO"),
    tags = this.tags,
    metadata = this.metadata
)

fun LogBatch.toLogBatchDTO() = LogBatchDTO(
    id = this.application.id.toString(),
    logs = this.logs,
    index = this.application.index
)
