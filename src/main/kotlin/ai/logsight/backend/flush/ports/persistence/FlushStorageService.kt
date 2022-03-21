package ai.logsight.backend.flush.ports.persistence

import ai.logsight.backend.flush.domain.Flush
import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.flush.domain.service.command.CreateFlushCommand
import java.util.*

interface FlushStorageService {
    fun saveFlush(createFlushCommand: CreateFlushCommand): Flush
    fun deleteFlush(flush: Flush)
    fun findFlushById(flushId: UUID): Flush
    fun findAllFlushByStatusAndApplicationId(status: FlushStatus, applicationId: UUID): List<Flush>
    fun updateFlushStatus(flush: Flush, status: FlushStatus): Flush
}
