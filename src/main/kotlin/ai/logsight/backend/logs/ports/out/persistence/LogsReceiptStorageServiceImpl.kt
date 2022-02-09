package ai.logsight.backend.logs.ports.out.persistence

import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.logs.domain.LogsReceipt
import ai.logsight.backend.logs.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.exceptions.LogsReceiptNotFoundException
import ai.logsight.backend.logs.extensions.toLogsReceipt
import org.springframework.stereotype.Service

@Service
class LogsReceiptStorageServiceImpl(
    private val logsReceiptRepository: LogsReceiptRepository
) : LogsReceiptStorageService {

    override fun saveLogReceipt(createLogsReceiptCommand: CreateLogsReceiptCommand): LogsReceipt {
        val logsReceiptEntity = LogsReceiptEntity(
            logsCount = createLogsReceiptCommand.logsCount,
            source = createLogsReceiptCommand.source,
            application = createLogsReceiptCommand.application.toApplicationEntity()
        )
        return logsReceiptRepository.save(logsReceiptEntity).toLogsReceipt()
    }

    override fun findLogReceiptById(logReceiptId: Long): LogsReceipt {
        return findLogReceiptByIdPrivate(logReceiptId).toLogsReceipt()
    }

    private fun findLogReceiptByIdPrivate(logReceiptId: Long): LogsReceiptEntity {
        return logsReceiptRepository.findById(logReceiptId).orElseThrow { LogsReceiptNotFoundException() }
    }
}
