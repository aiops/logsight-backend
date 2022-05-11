package ai.logsight.backend.logs.ingestion.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.LogMessage
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.dto.LogSinglesDTO
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ingestion.ports.web.requests.SendLogMessage
import ai.logsight.backend.logs.ingestion.ports.web.responses.LogsReceiptResponse
import ai.logsight.backend.logs.utils.LogFileReader
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
        val logBatchDTO = LogBatchDTO(
            user = userStorageService.findUserByEmail(authentication.name),
            application = applicationStorageService.findApplicationById(logListRequest.applicationId),
            tag = logListRequest.tag,
            logs = logListRequest.logs,
            source = LogDataSources.REST_BATCH
        )
        val logsReceipt = logsService.processLogBatch(logBatchDTO)
        return LogsReceiptResponse(
            logsReceipt.id, logsReceipt.logsCount, logsReceipt.source, logsReceipt.application.id
        )
    }

    @ApiOperation("Send list of log messages")
    @PostMapping("/singles")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogSingles(
        authentication: Authentication,
        @RequestBody @Valid logListRequest: MutableList<SendLogMessage>
    ): List<LogsReceiptResponse> {
        val logSinglesDTO = LogSinglesDTO(
            user = userStorageService.findUserByEmail(authentication.name),
            logs = logListRequest,
            source = LogDataSources.REST_BATCH
        )
        val logsReceipts = logsService.processLogSingles(logSinglesDTO)
        return logsReceipts.map { logsReceipt ->
            LogsReceiptResponse(
                logsReceipt.id,
                logsReceipt.logsCount,
                logsReceipt.source,
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
    ): LogsReceiptResponse {
        val logBatchDTO = LogBatchDTO(
            user = userStorageService.findUserByEmail(authentication.name),
            application = applicationStorageService.findApplicationById(applicationId),
            tag = tag,
            logs = LogFileReader().readFile(file.name, file.inputStream),
            source = LogDataSources.FILE
        )
        val logsReceipt = logsService.processLogBatch(logBatchDTO)
        return LogsReceiptResponse(
            logsReceipt.id, logsReceipt.logsCount, logsReceipt.source, logsReceipt.application.id
        )
    }
}
