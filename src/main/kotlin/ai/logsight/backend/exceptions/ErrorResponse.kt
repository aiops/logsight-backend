package ai.logsight.backend.exceptions

import org.springframework.http.HttpStatus

data class ErrorResponse(
    private val httpStatus: HttpStatus,
    val details: String
) {
    val code: Int = httpStatus.value()
    val description: String = httpStatus.reasonPhrase
}
