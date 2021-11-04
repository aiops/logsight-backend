package com.loxbear.logsight.services

import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.log.LogMessage
import com.loxbear.logsight.models.log.LogMessageException
import com.loxbear.logsight.repositories.kafka.LogRepository
import org.springframework.stereotype.Service
import java.util.logging.Logger


@Service
class LogService(
    val logRepository: LogRepository
) {
    val log: Logger = Logger.getLogger(LogService::class.java.toString())

    fun processFileContent(
        authMail: String,
        appID: Long,
        fileContent: String,
        logType: LogFileTypes
    ) {
        if (fileContent.isEmpty()) {
            val msg = "Received log file is empty."
            log.warning(msg)
            throw LogMessageException(msg)
        }

        logRepository.toKafka(authMail, appID, logType, processFileContent(fileContent))
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
                if (buffer.isNotEmpty())
                    logMessages.add(LogMessage(buffer.trim()))
                it.trim().plus(" ")
            }
        }
        if (buffer.isNotEmpty()) {
            logMessages.add(LogMessage(buffer.trim()))
        }
        return logMessages
    }
}
