package ai.logsight.backend.logs.ingestion.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.dto.LogEventsDTO
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.logs.ingestion.ports.web.responses.LogsReceiptResponse
import ai.logsight.backend.logs.ingestion.ports.web.responses.NotImplementedResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Api(tags = ["Logs"], description = "Send logs")
@RestController
@RequestMapping("/api/v1/logs")
class LogsController(
    val logsService: LogIngestionService,
    val userStorageService: UserStorageService,
    val applicationStorageService: ApplicationStorageService
) {

    @ApiOperation("Send list of log messages for analysis")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(
        authentication: Authentication,
        @RequestBody @Valid logListRequest: SendLogListRequest
    ): LogsReceiptResponse {
        val logs = logListRequest.logs.map { LogsightLog(tags = listOf(logListRequest.tag), event = it) }
        val logBatchDTO = LogBatch(
            application = applicationStorageService.findApplicationById(logListRequest.applicationId),
            logs = logs
        )
        val logsReceipt = logsService.processLogBatch(logBatchDTO)
        return LogsReceiptResponse(
            logsReceipt.id, logsReceipt.logsCount, logsReceipt.application.id
        )
    }

    @ApiOperation("Send list of log messages")
    @PostMapping("/singles")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogSingles(
        authentication: Authentication,
        @RequestBody @Valid logListRequest: MutableList<SendLogMessage>
    ): List<LogsReceiptResponse> {
        val logSinglesDTO = LogEventsDTO(
            user = userStorageService.findUserByEmail(authentication.name),
            logs = logListRequest,
        )
        val logsReceipts = logsService.processLogEvents(logSinglesDTO)
        return logsReceipts.map { logsReceipt ->
            LogsReceiptResponse(
                logsReceipt.id,
                logsReceipt.logsCount,
                logsReceipt.application.id
            )
        }
    }

    @ApiOperation("Send log file for analysis")
    @PostMapping("/file")
    fun uploadFile(
        authentication: Authentication,
        @RequestPart("file") @NotNull(message = "file must not be empty.") file: MultipartFile,
        @RequestParam("applicationId") @NotNull(message = "applicationId must not be empty.") applicationId: UUID,
        @RequestParam("tag", defaultValue = "default") tag: String,
    ): NotImplementedResponse {
        return NotImplementedResponse("Not implemented.")
    }
}
