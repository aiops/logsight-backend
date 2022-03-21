package ai.logsight.backend.flush.domain.service

import ai.logsight.backend.flush.domain.Flush
import ai.logsight.backend.flush.domain.service.command.CreateFlushCommand
import ai.logsight.backend.flush.domain.service.command.UpdateFlushStatusCommand
import ai.logsight.backend.flush.domain.service.query.FindFlushQuery

interface FlushService {
    fun createFlush(createFlushCommand: CreateFlushCommand): Flush
    fun findFlush(findFlushQuery: FindFlushQuery): Flush
    fun updateFlushStatus(updateFlushStatusCommand: UpdateFlushStatusCommand): Flush?
}
