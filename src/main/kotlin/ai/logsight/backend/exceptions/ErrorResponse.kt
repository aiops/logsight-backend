package ai.logsight.backend.exceptions

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import java.util.*

data class ErrorResponse(
    private val httpStatus: HttpStatus,
    val message: String,
    val path: String,
    var stackTrace: String? = null
) {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss")
    val timestamp: Date = Date()
    val status: Int = httpStatus.value()
    val error: String = httpStatus.reasonPhrase
}
