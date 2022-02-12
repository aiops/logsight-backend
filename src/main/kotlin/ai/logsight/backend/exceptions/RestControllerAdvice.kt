package ai.logsight.backend.exceptions

import ai.logsight.backend.application.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationRemoteException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.charts.exceptions.InvalidFeatureException
import ai.logsight.backend.logs.exceptions.LogFileIOException
import ai.logsight.backend.token.exceptions.InvalidTokenException
import ai.logsight.backend.token.exceptions.InvalidTokenTypeException
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.exceptions.TokenNotFoundException
import ai.logsight.backend.users.exceptions.*
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.elasticsearch.ElasticsearchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.naming.AuthenticationException
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class RestControllerAdvice {

    @ExceptionHandler(
        BadCredentialsException::class, AuthenticationException::class
    )
    fun handleUnauthorizedException(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.UNAUTHORIZED, request, e)
    }

    @ExceptionHandler(
        UserNotActivatedException::class,
        UserExistsException::class,
        EmailExistsException::class,
        TokenExpiredException::class,
        EmailExistsException::class,
        ApplicationAlreadyCreatedException::class,
        ApplicationStatusException::class,
        UserAlreadyActivatedException::class
    )
    fun handleConflictException(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.CONFLICT, request, e)
    }

    @ExceptionHandler(
        RuntimeException::class,
        ElasticsearchException::class,
        MailException::class,
        LogFileIOException::class,
        Exception::class // Wildcard
        LogFileIOException::class,
        ApplicationRemoteException::class
    )
    fun handleInternalServerError(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e)
    }

    @ExceptionHandler(
        InvalidFeatureException::class,
        PasswordsNotMatchException::class,
        UserNotFoundException::class,
        InvalidTokenException::class,
        InvalidTokenTypeException::class,
        TokenNotFoundException::class,
        ApplicationNotFoundException::class,
        MissingKotlinParameterException::class,
    )
    fun handleBadRequest(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.BAD_REQUEST, request, e)
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class
    )
    fun handleValidationException(
        request: HttpServletRequest,
        e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
//        val messages = e.fieldErrors.map { x -> x.defaultMessage }.joinToString(separator = ",", prefix = "Errors: ")
        val message = e.fieldErrors[0].defaultMessage
        return generateErrorResponse(
            HttpStatus.BAD_REQUEST, request, e, message.toString()
        )
    }

    private fun generateErrorResponse(
        status: HttpStatus,
        request: HttpServletRequest,
        e: Exception,
        message: String = e.message.toString()
    ): ResponseEntity<ErrorResponse> {
        // converting the exception stack trace to a string
        val stackTrace = e.stackTraceToString()
        val requestPath = request.requestURI
        // example: logging the stack trace
        // log.debug(stackTrace)

//        environment - based logic
//        val stackTraceMessage = when (System.getenv("ENV").toUpperCase()) {
//            "STAGING" -> stackTrace // returning the stack trace
//            "PRODUCTION" -> null // returning no stack trace
//            else -> stackTrace // default behavior
//        }

        return ResponseEntity(ErrorResponse(status, message, requestPath, stackTrace), status)
    }
}
