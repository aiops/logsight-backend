package ai.logsight.backend.ingestion.domain

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.exceptions.FileUploadException
import ai.logsight.backend.ingestion.ports.out.Sink
import java.util.logging.Logger

class IngestionServiceImpl(
    private val applicationService: ApplicationLifecycleService,
    private val sink: Sink
) : IngestionService {
    val log: Logger = Logger.getLogger(IngestionService::class.java.toString())

    override fun uploadFile(fileContent: String) {
        if (fileContent.isEmpty()) {
            log.warning("Received log file is empty.")
            throw FileUploadException()
        }
//        sink.sendData(processFileContent(fileContent))
    }

    private fun processFileContent(
        fileContent: String
    ): List<LogMessage> {
        val logMessages = mutableListOf<LogMessage>()
        var buffer = ""
        fileContent.lines().filter { it.isNotEmpty() }.forEach {
            buffer = if (it.first().isWhitespace()) {
                buffer.plus(it.trim().plus(" "))
            } else {
                if (buffer.isNotEmpty()) logMessages.add(LogMessage(buffer.trim()))
                it.trim().plus(" ")
            }
        }
        if (buffer.isNotEmpty()) {
            logMessages.add(LogMessage(buffer.trim()))
        }
        return logMessages
    }
}
