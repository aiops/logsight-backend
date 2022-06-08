package ai.logsight.backend.logs.ingestion.extensions

import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.exceptions.InvalidIdException

operator fun LogBatchDTO.plus(batch: LogBatchDTO): LogBatchDTO {
    if (this.id == batch.id) {
        return LogBatchDTO(
            this.id,
            index = this.index,
            logs = this.logs + batch.logs,
        )
    } else {
        throw InvalidIdException("Combine batches with different IDs")
    }
}
