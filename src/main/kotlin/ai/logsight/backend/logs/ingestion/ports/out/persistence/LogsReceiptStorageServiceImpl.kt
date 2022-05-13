package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.exceptions.LogsReceiptNotFoundException
import ai.logsight.backend.logs.ingestion.extensions.toLogsReceipt
import org.springframework.stereotype.Service
import java.util.*

@Service
class LogsReceiptStorageServiceImpl(
    private val logsReceiptRepository: LogsReceiptRepository,
    private val applicationRepository: ApplicationRepository
) : LogsReceiptStorageService {
    override fun saveLogsReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogsReceipt {
        val logsReceiptEntity = LogsReceiptEntity(
            logsCount = createLogsReceiptCommand.logsCount,
            application = applicationRepository.findById(createLogsReceiptCommand.application.id)
                .orElseThrow { ApplicationNotFoundException("Application ${createLogsReceiptCommand.application.id} does not exist for user.") }

        )
        return logsReceiptRepository.save(logsReceiptEntity)
            .toLogsReceipt()
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
