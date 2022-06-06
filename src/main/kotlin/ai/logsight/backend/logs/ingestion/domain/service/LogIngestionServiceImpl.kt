package ai.logsight.backend.logs.ingestion.domain.service

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.extensions.toLogBatchDTO
import ai.logsight.backend.logs.extensions.toLogsightLog
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.service.command.CreateLogsReceiptCommand
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import org.springframework.stereotype.Service

@Service
class LogIngestionServiceImpl(
    private val applicationStorageService: ApplicationStorageService,
    private val applicationLifecycleService: ApplicationLifecycleService,
    private val logsReceiptStorageService: LogsReceiptStorageService,
    private val logSink: LogSink,
) : LogIngestionService {

    override fun processLogBatch(logBatch: LogBatch): LogsReceipt {
        val createLogsReceiptCommand = CreateLogsReceiptCommand(
            logsCount = logBatch.logs.size,
            application = logBatch.application
        )
        val receipt = logsReceiptStorageService.saveLogsReceipt(createLogsReceiptCommand)
        logBatch.logs = logBatch.logs.map {
            LogsightLog(
                it.id,
                it.event,
                it.metadata,
                it.tags.plus(mapOf("applicationName" to logBatch.application.name))
            )
        }
        logSink.sendLogBatch(logBatch.toLogBatchDTO()) // toLogBatchDTO
        return receipt
    }

    override fun processLogEvents(logEventsDTO: LogEventsDTO): List<LogsReceipt> {
        val logBatches = mapToLogBatches(logEventsDTO)
        return logBatches.map { processLogBatch(it) }
    }

    private fun mapToLogBatches(logEventsDTO: LogEventsDTO): List<LogBatch> {
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
                val application = applicationLifecycleService.createApplication(
                    CreateApplicationCommand(
                        applicationName = grouped.key!!,
                        user = logEventsDTO.user,
                        displayName = grouped.key!!
                    )
                )
                LogBatch(
                    application = application,
                    logs = grouped.value.map { it.toLogsightLog() }
                )
            }
        // Combine batches per application ID and send them (the plus operator was override)
        return knownId + unknownId
    }
}
