package ai.logsight.backend.logs.utils

import java.io.InputStream

class LogFileReader {

    fun readFile(fileName: String, inputStream: InputStream): List<String> {
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
    private fun logLinesToList(fileContent: String): List<String> {
        val logMessages = mutableListOf<String>()
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
                        stringBuilder.toString()
                            .trim()
                    )
                    stringBuilder.clear()
                }
            }
        if (stringBuilder.isNotEmpty())
            logMessages.add(
                stringBuilder.toString()
                    .trim()
            )
        return logMessages
    }
}
