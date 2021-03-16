package utils

//import java.nio.file.Files
//import java.nio.file.Paths
import org.springframework.util.ResourceUtils
import java.io.BufferedReader
import java.io.File

import java.io.InputStream
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


class UtilsService {


    companion object {

        fun readFileAsString(fileName: String?): String {
            val inStream = getResourceFileAsInputStream(fileName)
            return if (inStream != null) {
                val reader = BufferedReader(InputStreamReader(inStream))
                reader.lines().collect(Collectors.joining(System.lineSeparator())) as String
            } else {
                throw RuntimeException("resource not found")
            }
        }

        private fun getResourceFileAsInputStream(fileName: String?): InputStream {
            val classLoader = Asd::class.java.classLoader
            return classLoader.getResourceAsStream(fileName)
        }

    }


}