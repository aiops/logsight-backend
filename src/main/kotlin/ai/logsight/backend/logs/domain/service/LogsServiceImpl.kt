package ai.logsight.backend.logs.domain.service

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.exceptions.FileUploadException
import ai.logsight.backend.logs.domain.LogContext
import ai.logsight.backend.users.domain.service.UserService
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class LogsServiceImpl(
    userService: UserService,
    appService: ApplicationLifecycleService
) : LogsService {
    val log: Logger = Logger.getLogger(LogsService::class.java.toString())

    override fun forwardLogs(logContext: LogContext): Int {
        TODO("Not yet implemented")
    }

    override fun uploadFile(fileContent: String) {
        if (fileContent.isEmpty()) {
            log.warning("Received log file is empty.")
            throw FileUploadException()
        }
//        processFileContent(fileContent).forEach { item -> sink.sendData(item) }
    }

    private fun processFileContent(
        fileContent: String
    ): List<String> {
        val logMessages = mutableListOf<String>()
        var buffer = ""
        fileContent.lines().filter { it.isNotEmpty() }.forEach {
            buffer = if (it.first().isWhitespace()) {
                buffer.plus(it.trim().plus(" "))
            } else {
                if (buffer.isNotEmpty()) logMessages.add(buffer.trim())
                it.trim().plus(" ")
            }
        }
        if (buffer.isNotEmpty()) {
            logMessages.add(buffer.trim())
        }
        return logMessages
    }
}
