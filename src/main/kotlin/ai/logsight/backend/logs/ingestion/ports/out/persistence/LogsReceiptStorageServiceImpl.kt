package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.exceptions.LogsReceiptNotFoundException
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceipt
import org.springframework.stereotype.Service
import java.util.*

@Service
class LogsReceiptStorageServiceImpl(
    private val logsReceiptRepository: LogsReceiptRepository,
    private val applicationStorageService: ApplicationStorageService
) : LogsReceiptStorageService {
    override fun saveLogsReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogsReceipt {
        val logsReceiptEntity = LogsReceiptEntity(
            logsCount = createLogsReceiptCommand.logsCount,
            application = applicationStorageService
                .findApplicationById(createLogsReceiptCommand.application.id)
                .toApplicationEntity()
        )
        return logsReceiptRepository.save(logsReceiptEntity).toLogsReceipt()
    }

    override fun findLogsReceiptById(logReceiptId: UUID): LogsReceipt {
        return findLogsReceiptByIdPrivate(logReceiptId).toLogsReceipt()
    }

    override fun updateLogsCount(logsReceipt: LogsReceipt, logsCount: Int): LogsReceipt {
        val logsReceiptEntity = findLogsReceiptByIdPrivate(logsReceipt.id)
        logsReceiptEntity.logsCount = logsCount
        return logsReceiptRepository.save(logsReceiptEntity)
            .toLogsReceipt()
    }

    private fun findLogsReceiptByIdPrivate(logsReceiptId: UUID): LogsReceiptEntity {
        return logsReceiptRepository.findById(logsReceiptId)
            .orElseThrow { LogsReceiptNotFoundException("LogRequest receipt with id $logsReceiptId is not found") }
    }
}
