package ai.logsight.backend.logs.utils

import ai.logsight.backend.logs.domain.LogsightLog
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream

class LogFileReader {

    private fun readFile(fileName: String, inputStream: InputStream): List<LogsightLog> {
        val fileContent = readFileContent(fileName, inputStream)
        return logLinesToList(fileContent)
    }

    fun readDemoFile(fileName: String, inputStream: InputStream): List<LogsightLog> {
        val version = fileName.split("-")[1]
        val service = fileName.split("-")[0]
        val logs = readFile(fileName, inputStream)
        return logs.map {
            LogsightLog(
                message = it.message,
                timestamp = it.timestamp,
                level = it.level,
                tags = mapOf("version" to version, "service" to service)
            )
        }
    }

    private fun readFileContent(fileName: String, inputStream: InputStream): String =
        try {
            inputStream.readBytes()
                .toString(Charsets.UTF_8)
        } catch (e: Exception) {
            throw LogFileIOException("Error while reading file content of file $fileName. Reason: ${e.message}")
        }

    private fun logLinesToList(fileContent: String): List<LogsightLog> {
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        val logMessages = fileContent.lines().filter { it.isNotEmpty() }.map {
            mapper.readValue(it, LogsightLog::class.java)
        }
        return logMessages
    }
}
