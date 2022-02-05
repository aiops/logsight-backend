package ai.logsight.backend.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.naming.AuthenticationException

@ControllerAdvice
class RestControllerAdvice {
    @ExceptionHandler(LogsightApplicationException::class)
    fun handleLogsightApplicationException(logsightApplicationException: LogsightApplicationException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, logsightApplicationException.message.toString())

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(
        value = HttpStatus.UNAUTHORIZED,
        reason = "Email or password are not correct."
    )
    fun handleBadCredentialsException(logsightApplicationException: BadCredentialsException) {
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(authenticationException: AuthenticationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.UNAUTHORIZED, authenticationException.message.toString())

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(UserExistsException::class)
    @ResponseStatus(
        value = HttpStatus.CONFLICT,
        reason = "User already exists. Please login."
    )
    fun handleUserExistsException(userExistsException: UserExistsException) {
    }

    @ExceptionHandler(UserNotActivatedException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "User has not been activated.")
    fun handleUserNotActivated(userNotActivatedException: UserNotActivatedException) {
    }

    @ExceptionHandler(EmailExistsException::class)
    fun handleEmailExistsException(emailExistsException: EmailExistsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.CONFLICT, emailExistsException.message.toString())

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(InvalidFeatureException::class)
    fun handleInvalidFeature(invalidFeatureException: InvalidFeatureException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.BAD_REQUEST, invalidFeatureException.message.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(PasswordsNotMatchException::class)
    fun handlePasswordsNotMatch(passwordsNotMatchException: PasswordsNotMatchException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(HttpStatus.BAD_REQUEST, passwordsNotMatchException.message.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}
