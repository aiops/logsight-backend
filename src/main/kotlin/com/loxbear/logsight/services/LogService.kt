package com.loxbear.logsight.services

import com.loxbear.logsight.controllers.FileUploadController
import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.log.*
import com.loxbear.logsight.repositories.kafka.LogRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
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

        val logs = when (logType) {
            LogFileTypes.LOGSIGHT_JSON -> processJsonFile(fileContent)
            else -> processDefaultFile(fileContent)

        }
        logRepository.toKafka(authMail, appID, logType, logs)
    }

    private fun processJsonFile(
        fileContent: String
    ): List<LogMessageLogsight> {
        val failedToParse = mutableListOf<String>()
        val logs = mutableListOf<LogMessageLogsight>()
        val jsonArray = strToJsonArray(fileContent)
        jsonArray.map {
            try {
                Json.decodeFromJsonElement<LogMessageLogsight>(it)
            } catch (e: SerializationException) {
                failedToParse.add("Failed to parse log message. Reason: ${e.message}")
            }
        }.filterIsInstanceTo(logs)

        if (failedToParse.size > 0) {
            log.warning("Failed to parse ${failedToParse.size} log entries.")
            log.fine(failedToParse.joinToString(separator = "\n"))
        }
        return logs
    }

    private fun strToJsonArray(jsonString: String): JsonArray {
        val jsonLogs = try {
            Json.parseToJsonElement(jsonString)
        } catch (e: SerializationException) {
            log.warning(e.message)
            throw LogMessageException(e)
        }

        val jsonArray = try {
            jsonLogs
                .jsonObject["log-messages"]!! //Catch null pointer exception
                .jsonArray
        } catch (e: NullPointerException) {
            val msg = "JSON object requires \"log-messages\" as key."
            log.warning("$msg $e")
            throw LogMessageException(msg, e)
        } catch (e: IllegalArgumentException) {
            val msg = "JSON element \"log-messages\" must be a json array."
            log.warning("$msg $e")
            throw LogMessageException(msg, e)
        }

        return jsonArray
    }

    private fun processDefaultFile(
        fileContent: String
    ): List<LogMessage> {
        return fileContent.lines().filter { x -> x.isNotEmpty() }.map { LogMessageBasic(it) }
    }
}