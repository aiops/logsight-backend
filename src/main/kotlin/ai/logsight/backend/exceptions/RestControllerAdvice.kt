package ai.logsight.backend.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.naming.AuthenticationException

@ControllerAdvice
class RestControllerAdvice {
    @ExceptionHandler(LogsightApplicationException::class)
    fun handleLogsightApplicationException(logsightApplicationException: LogsightApplicationException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, logsightApplicationException.message.toString())

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    @ExceptionHandler(AuthenticationException::class)
    fun handleLogsightAuthenticationException(authenticationException: AuthenticationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.UNAUTHORIZED, authenticationException.message.toString())

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(EmailExistsException::class)
    fun handleLogsightEmailExistsException(emailExistsException: EmailExistsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.CONFLICT, emailExistsException.message.toString())

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(InvalidFeatureException::class)
    fun handleLogsightInvalidFeature(invalidFeatureException: InvalidFeatureException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.BAD_REQUEST, invalidFeatureException.message.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(PasswordsNotMatchException::class)
    fun handleLogsightPasswordsNotMatch(passwordsNotMatchException: PasswordsNotMatchException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.BAD_REQUEST, passwordsNotMatchException.message.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}
