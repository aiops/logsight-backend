package ai.logsight.backend.logs.utils

import ai.logsight.backend.logs.domain.LogEvent
import ai.logsight.backend.logs.domain.LogsightLog
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream

class LogFileReader {

    fun readFile(fileName: String, inputStream: InputStream): List<LogEvent> {
        val fileContent = readFileContent(fileName, inputStream)
        return logLinesToList(fileContent)
    }

    fun readDemoFile(fileName: String, inputStream: InputStream): List<LogsightLog> {
        val logs = readFile(fileName, inputStream)
        return logs.map { LogsightLog(event = it, tags = mapOf("default" to "fileName")) }
    }

    private fun readFileContent(fileName: String, inputStream: InputStream): String =
        try {
            inputStream.readBytes()
                .toString(Charsets.UTF_8)
        } catch (e: Exception) {
            throw LogFileIOException("Error while reading file content of file $fileName. Reason: ${e.message}")
        }

    private fun logLinesToList(fileContent: String): List<LogEvent> {
        val mapper = ObjectMapper().registerModule(KotlinModule())!!
        val logMessages = fileContent.lines().filter { it.isNotEmpty() }.map {
            mapper.readValue<LogEvent>(it, LogEvent::class.java)
        }
        return logMessages
    }
}
