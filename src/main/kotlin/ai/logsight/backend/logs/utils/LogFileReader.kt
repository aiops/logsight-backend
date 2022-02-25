package ai.logsight.backend.logs.utils

import ai.logsight.backend.logs.domain.Log
import ai.logsight.backend.logs.ingestion.ports.web.requests.LogRequest
import java.io.InputStream

class LogFileReader {

    fun readFile(fileName: String, inputStream: InputStream): List<LogRequest> {
        val fileContent = readFileContent(fileName, inputStream)
        return logLinesToList(fileContent)
    }

    private fun readFileContent(fileName: String, inputStream: InputStream): String =
        try {
            inputStream.readBytes()
                .toString(Charsets.UTF_8)
        } catch (e: Exception) {
            throw LogFileIOException("Error while reading file content of file $fileName. Reason: ${e.message}")
        }

    // TODO (This must be tested)
    private fun logLinesToList(fileContent: String): List<LogRequest> {
        val logMessages = mutableListOf<LogRequest>()
        val stringBuilder = StringBuilder()
        // Filters empty lines and appends multiline logs to one line based on heuristic that
        // multiline logs start with spaces
        fileContent.lines()
            .filter { it.isNotEmpty() }
            .forEach {
                stringBuilder.append(
                    it.trim()
                        .plus(" ")
                )
                if (!it.first()
                    .isWhitespace()
                ) {
                    logMessages.add(
                        LogRequest(
                            null,
                            message = stringBuilder.toString()
                                .trim(),
                            null, null
                        )

                    )
                    stringBuilder.clear()
                }
            }
        if (stringBuilder.isNotEmpty())
            logMessages.add(
                LogRequest(
                    null,
                    message = stringBuilder.toString()
                        .trim(),
                    null, null
                )
            )
        return logMessages
    }
}
