package ai.logsight.backend.logs.utils

import ai.logsight.backend.logs.domain.LogMessage
import java.io.InputStream

class LogFileReader {

    fun readFile(fileName: String, inputStream: InputStream): List<LogMessage> {
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
    private fun logLinesToList(fileContent: String): List<LogMessage> {
        val logMessages = mutableListOf<LogMessage>()
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
                        LogMessage(
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
                LogMessage(
                    null,
                    message = stringBuilder.toString()
                        .trim(),
                    null, null
                )
            )
        return logMessages
    }
}
