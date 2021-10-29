package com.loxbear.logsight.services

import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.log.*
import com.loxbear.logsight.repositories.kafka.LogRepository
import org.springframework.stereotype.Service
import java.util.logging.Logger


@Service
class LogService(
    val logRepository: LogRepository
) {
    val log: Logger = Logger.getLogger(LogService::class.java.toString())

    fun processFile(
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

        logRepository.toKafka(authMail, appID, logType, processFile(fileContent))
    }

    private fun processFile(
        fileContent: String
    ): List<LogMessage> {
        return fileContent.lines().filter { x -> x.isNotEmpty() }.map { LogMessage(it) }
    }
}
