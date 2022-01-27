package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.logs.domain.LogContext
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ports.web.requests.SendSingleLogRequest
import ai.logsight.backend.logs.ports.web.responses.SendLogsResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/upload")
class LogsController(
    val logsService: LogsService
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(@Valid @RequestBody singleLogRequest: SendLogListRequest): SendLogsResponse {
        val logContext = LogContext(
            singleLogRequest.userId,
            singleLogRequest.appId,
            singleLogRequest.log
        )

        val numLogs = logsService.forwardLogs(logContext)

        return SendLogsResponse(numLogs)
    }

    @PostMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogFile(@Valid @RequestBody singleLogRequest: SendLogListRequest): SendLogsResponse {
        TODO("Not yet implemented")
    }
}
