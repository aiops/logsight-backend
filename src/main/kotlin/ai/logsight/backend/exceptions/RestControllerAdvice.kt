package ai.logsight.backend.exceptions

import ai.logsight.backend.application.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.charts.exceptions.InvalidFeatureException
import ai.logsight.backend.logs.exceptions.LogFileIOException
import ai.logsight.backend.token.exceptions.InvalidTokenException
import ai.logsight.backend.token.exceptions.InvalidTokenTypeException
import ai.logsight.backend.token.exceptions.TokenExpiredException
import ai.logsight.backend.token.exceptions.TokenNotFoundException
import ai.logsight.backend.users.exceptions.EmailExistsException
import ai.logsight.backend.users.exceptions.PasswordsNotMatchException
import ai.logsight.backend.users.exceptions.UserExistsException
import ai.logsight.backend.users.exceptions.UserNotActivatedException
import ai.logsight.backend.users.exceptions.UserNotFoundException
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

@ControllerAdvice class RestControllerAdvice {

    @ExceptionHandler(
        BadCredentialsException::class, AuthenticationException::class
    )
    fun handleUnauthorizedException(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorRepsonse> {
        return generateErrorResponse(HttpStatus.UNAUTHORIZED, request, e)
    }

    @ExceptionHandler(
        UserNotActivatedException::class,
        UserExistsException::class,
        EmailExistsException::class,
        TokenExpiredException::class,
        ApplicationAlreadyCreatedException::class,
        ApplicationStatusException::class

    )
    fun handleConflictException(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorRepsonse> {
        return generateErrorResponse(HttpStatus.CONFLICT, request, e)
    }

    @ExceptionHandler(
        RuntimeException::class,
        ElasticsearchException::class,
        MailException::class,
        LogFileIOException::class
    )
    fun handleInternalServerError(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorRepsonse> {
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
    )
    fun handleBadRequest(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorRepsonse> {
        return generateErrorResponse(HttpStatus.BAD_REQUEST, request, e)
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class
    )
    fun handleValidationException(
        request: HttpServletRequest,
        e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorRepsonse> {
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
    ): ResponseEntity<ErrorRepsonse> {
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

        return ResponseEntity(ErrorRepsonse(status, message, requestPath, stackTrace), status)
    }
}
