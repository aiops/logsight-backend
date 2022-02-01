package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.logs.domain.LogDTO
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.ports.web.requests.LogsRequest
import ai.logsight.backend.logs.ports.web.responses.LogsResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/logs")
class LogsController(
    val logsService: LogsService,
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(
        authentication: Authentication,
        @Valid @RequestBody logsRequest: LogsRequest
    ): LogsResponse {
        // TODO(Write an exception handling)
        val logDto = LogDTO(authentication.name, logsRequest.appId, logsRequest.logs)
        logsService.forwardLogs(logDto)
        return LogsResponse()
    }

    @PostMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogFile(@Valid @RequestBody singleLogRequest: LogsRequest): LogsResponse {
        TODO("Not yet implemented")
    }
}
