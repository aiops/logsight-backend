package utils

import java.nio.file.Files
import java.nio.file.Paths

class UtilsService {
    companion object {
        fun readFileAsString(path: String): String {
            return String(Files.readAllBytes(Paths.get(path)))
        }
    }
}