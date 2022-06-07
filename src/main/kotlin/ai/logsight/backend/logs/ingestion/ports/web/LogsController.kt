package ai.logsight.backend.logs.ingestion.ports.web

import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogListDTO
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.logs.ingestion.ports.web.responses.LogsReceiptResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["Logs"], description = "Send logs")
@RestController
@RequestMapping("/api/v1/logs")
class LogsController(
    val logsService: LogIngestionService,
    val userStorageService: UserStorageService
) {

    @ApiOperation("Send list of log messages for analysis")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(
        authentication: Authentication,
        @RequestBody @Valid logListRequest: SendLogListRequest
    ): LogsReceiptResponse {

        val logEventsDTO = LogListDTO(
            index = userStorageService.findUserByEmail(authentication.name).key,
            logs = logListRequest.logs,
            tags = logListRequest.tags
        )
        val logsReceipt = logsService.processLogList(logEventsDTO)
        return LogsReceiptResponse(
            logsReceipt.id, logsReceipt.logCount, logsReceipt.processedLogCount,
            logsReceipt.batchId,
            logsReceipt.status
        )
    }

    @ApiOperation("Send list of log messages")
    @PostMapping("/singles")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogSingles(
        authentication: Authentication,
        @RequestBody @Valid logListRequest: MutableList<SendLogMessage>
    ): LogsReceiptResponse {
        val logSinglesDTO = LogEventsDTO(
            index = userStorageService.findUserByEmail(authentication.name).key,
            logs = logListRequest,
        )
        val logsReceipt = logsService.processLogEvents(logSinglesDTO)
        return LogsReceiptResponse(
            logsReceipt.id,
            logsReceipt.logCount,
            logsReceipt.processedLogCount,
            logsReceipt.batchId,
            logsReceipt.status
        )
    }
}
