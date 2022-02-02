package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogContext
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ports.web.requests.SendSingleLogRequest
import ai.logsight.backend.logs.ports.web.responses.SendLogsResponse
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/logs")
class LogsController(
    val logsService: LogsService,
    val userService: UserService,
    val applicationStorageService: ApplicationStorageService
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(authentication: Authentication, @Valid @RequestBody logRequest: SendLogListRequest): SendLogsResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        val application = applicationStorageService.findApplicationById(logRequest.appId)

        val logContext = LogContext(
            user.id,
            logRequest.appId,
            logRequest.logs
        )

        val numLogs = logsService.forwardLogs(logContext)

        return SendLogsResponse(description = "Log batch received successfully", applicationId = application.id, tag = logRequest.tag)
    }

//    @PostMapping("/file")
//    @ResponseStatus(HttpStatus.OK)
//    fun sendLogFile(@Valid @RequestBody singleLogRequest: SendLogListRequest): SendLogsResponse {
//        TODO("Not yet implemented")
//    }
}
