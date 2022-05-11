package ai.logsight.backend.flush.ports.persistence

import ai.logsight.backend.logs.ingestion.extensions.toLogsReceiptEntity
import ai.logsight.backend.flush.domain.Flush
import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.flush.domain.service.command.CreateFlushCommand
import ai.logsight.backend.flush.exceptions.FlushNotFoundException
import ai.logsight.backend.flush.extensions.toFlush
import ai.logsight.backend.flush.extensions.toFlushEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class FlushStorageServiceImpl(
    private val flushRepository: FlushRepository
) : FlushStorageService {

    override fun saveFlush(createFlushCommand: CreateFlushCommand): Flush {
        val flushEntity = FlushEntity(
            status = FlushStatus.PENDING,
            logsReceipt = createFlushCommand.logsReceipt.toLogsReceiptEntity()
        )
        return flushRepository.save(flushEntity)
            .toFlush()
    }

    override fun deleteFlush(flush: Flush) =
        flushRepository.delete(flush.toFlushEntity())

    override fun findAllFlushByStatusAndApplicationId(
        status: FlushStatus,
        applicationId: UUID
    ): List<Flush> =
        flushRepository.findAllByStatusAndLogsReceipt_Application_Id(status, applicationId)
            .map(FlushEntity::toFlush)

    override fun findFlushById(flushId: UUID): Flush =
        findFlushByIdPrivate(flushId).toFlush()

    override fun updateFlushStatus(flush: Flush, status: FlushStatus): Flush {
        val flushEntity = flush.toFlushEntity()
        flushEntity.status = status
        return flushRepository.save(flushEntity)
            .toFlush()
    }

    private fun findFlushByIdPrivate(flushId: UUID): FlushEntity =
        flushRepository.findById(flushId)
            .orElseThrow { FlushNotFoundException("Flush entity with ID $flushId does not exist.") }
}
