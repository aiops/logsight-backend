package ai.logsight.backend.exceptions

import ai.logsight.backend.application.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationRemoteException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.charts.exceptions.InvalidFeatureException
import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.exceptions.RemoteCompareException
import ai.logsight.backend.logs.ingestion.exceptions.LogQueueCapacityLimitReached
import ai.logsight.backend.logs.ingestion.exceptions.LogsReceiptNotFoundException
import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSinkException
import ai.logsight.backend.logs.utils.LogFileIOException
import ai.logsight.backend.token.exceptions.InvalidTokenException
import ai.logsight.backend.token.exceptions.InvalidTokenTypeException
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.exceptions.TokenNotFoundException
import ai.logsight.backend.users.exceptions.*
import ai.logsight.backend.users.ports.out.external.exceptions.ExternalServiceException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.elasticsearch.ElasticsearchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mail.MailException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.naming.AuthenticationException
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class RestControllerAdvice {

    private val logger: Logger = LoggerImpl(RestControllerAdvice::class.java)

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
        ApplicationAlreadyCreatedException::class,
        ApplicationStatusException::class,
        UserAlreadyActivatedException::class,
    )
    fun handleConflictException(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.CONFLICT, request, e)
    }

    @ExceptionHandler(
        RuntimeException::class,
        ElasticsearchException::class,
        MailException::class,
        LogFileIOException::class,
        ApplicationRemoteException::class,
        Exception::class, // Wildcard,
        RemoteCompareException::class,
        ExternalServiceException::class,
        LogSinkException::class
    )
    fun handleInternalServerError(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e)
    }

    @ExceptionHandler(
        InvalidFeatureException::class,
        PasswordsNotMatchException::class,
        InvalidTokenException::class,
        InvalidTokenTypeException::class,
        MissingKotlinParameterException::class,
        HttpMessageNotReadableException::class,
        IllegalArgumentException::class,
    )
    fun handleBadRequest(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.BAD_REQUEST, request, e)
    }

    @ExceptionHandler(
        UserNotFoundException::class,
        TokenNotFoundException::class,
        ApplicationNotFoundException::class,
        LogsReceiptNotFoundException::class,
    )
    fun handleNotFound(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.NOT_FOUND, request, e)
    }

    @ExceptionHandler(
        LogQueueCapacityLimitReached::class
    )
    fun handleTooManyRequests(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        return generateErrorResponse(HttpStatus.TOO_MANY_REQUESTS, request, e)
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
