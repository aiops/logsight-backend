package com.loxbear.logsight.services

import com.loxbear.logsight.entities.enums.LogFileType
import com.loxbear.logsight.models.LogMessage
import com.loxbear.logsight.models.LogMessageException
import com.loxbear.logsight.repositories.elasticsearch.LogRepository
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.logging.Logger

val log: Logger = Logger.getLogger("LogService")

@Service
class LogService(
    val logRepository: LogRepository
) {

    fun processFile(
        authID: String,
        appID: String,
        file: MultipartFile,
        type: LogFileType
    ) {
        if (file.isEmpty) {
            throw LogMessageException("Uploaded file is empty.")
        }
        when(type) {
            LogFileType.LOSIGHT_JSON -> {
                val logs = processJsonFile(file)
                logRepository.sendToKafkaLogsightJSON(authID, appID, logs)
            }
            LogFileType.SYSLOG -> {
                val logs = processSyslogFile(file)
                logRepository.sendToKafkaSyslog(authID, appID, logs)
            }
        }
    }

    private fun processJsonFile(
        file: MultipartFile
    ): List<LogMessage> {

        val failedToParse = mutableListOf<String>()
        val logs = mutableListOf<LogMessage>()

        try {
            val jsonString = String(file.bytes, Charsets.UTF_8)
            val jsonLogs = Json.parseToJsonElement(jsonString)

            jsonLogs
                .jsonObject["log-messages"]!! //Catch null pointer exception
                .jsonArray.map {
                    try {
                        Json.decodeFromJsonElement<LogMessage>(it)
                    } catch (e: SerializationException) {
                        failedToParse.add("Failed to parse log message ${e.message}")
                    }
                }
                .filterIsInstanceTo(logs)
        } catch (e: SerializationException) {
            throw LogMessageException("Failed to parse JSON string. Invalid JSON format.", e)
        } catch (e: NullPointerException) {
            throw LogMessageException("JSON object requires \"log-messages\" as root key.", e)
        } catch (e: IllegalArgumentException) {
            throw LogMessageException("Not able deserialize JSON to LogMessage object.", e)
        }

        if(failedToParse.size > 0) {
            log.warning("Failed to parse ${failedToParse.size} log entries.")
            log.fine(failedToParse.joinToString(separator = "\n"))
        }
        return logs
    }

    private fun processSyslogFile(
        file: MultipartFile
    ): List<String>{

        val syslogString = String(file.bytes, Charsets.UTF_8)
        return syslogString.lines()
    }
}