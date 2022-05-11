package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.extensions.toLogsightLog
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.Sink
import org.springframework.stereotype.Service

@Service
class LogIngestionServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    private val logsightSink: Sink,
    val logsReceiptStorageService: LogsReceiptStorageService,
) : LogIngestionService {

    override fun processLogBatch(logBatch: LogBatch): LogsReceipt {
        val createLogsReceiptCommand = CreateLogsReceiptCommand(
            logsCount = logBatch.logs.size,
            application = logBatch.application
        )
        val receipt = logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand)
        this.logsightSink.sendBatch(logBatch.toLogBatchDTO()) // toLogBatchDTO
        return receipt
    }

    override fun processLogEvents(logEventsDTO: LogEventsDTO): List<LogsReceipt> {
        // Create batches for known Application IDs
        val knownId = logEventsDTO.logs.filter { it.applicationId != null } // filter only known applicationId
            .groupBy { it.applicationId }
            .map { grouped -> // create log batch DTO
                val application = applicationStorageService.findApplicationById(grouped.key!!)
                LogBatch(
                    application = application,
                    logs = grouped.value.map { it.toLogsightLog() }
                )
            }
        // Create batches for unknown applicationID
        val unknownId = logEventsDTO.logs.filter { it.applicationId == null }.groupBy { it.applicationName }
            .map { grouped ->
                val application = applicationStorageService.autoCreateApplication(grouped.key!!, logEventsDTO.user)
                LogBatch(
                    application = application,
                    logs = grouped.value.map { it.toLogsightLog() }
                )
            }
        // Combine batches per application ID and send them
        // TODO: This is optional, should we combine them or ignore this?
        return (knownId + unknownId).map { processLogBatch(it) }
    }
}
