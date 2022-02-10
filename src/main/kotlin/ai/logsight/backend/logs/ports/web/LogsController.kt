package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO
import ai.logsight.backend.logs.ports.web.requests.SendLogFileRequest
import ai.logsight.backend.logs.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ports.web.responses.LogsReceiptResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Logs"], description = " ")
@RestController
@RequestMapping("/api/v1/logs")
class LogsController(
    val logsService: LogsService,
    val userStorageService: UserStorageService,
    val applicationStorageService: ApplicationStorageService
) {

    @ApiOperation("Send list of log messages for analysis")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(
        authentication: Authentication,
        @Valid @RequestBody logListRequest: SendLogListRequest
    ): LogsReceiptResponse {
        val logBatchDTO = LogBatchDTO(
            user = userStorageService.findUserByEmail(authentication.name),
            application = applicationStorageService.findApplicationById(logListRequest.applicationId),
            tag = logListRequest.tag,
            logFormat = logListRequest.logFormats,
            logs = logListRequest.logs
        )
        val logsReceipt = logsService.processLogBatch(logBatchDTO)
        return LogsReceiptResponse(
            logsReceipt.application.id, logsReceipt.orderCounter, logsReceipt.logsCount, logsReceipt.source
        )
    }

    @ApiOperation("Send log file for analysis")
    @PostMapping("/file")
    fun uploadFile(
        authentication: Authentication,
        @Valid @RequestBody logFileRequest: SendLogFileRequest
    ): LogsReceiptResponse {
        val logFileDTO = LogFileDTO(
            user = userStorageService.findUserByEmail(authentication.name),
            application = applicationStorageService.findApplicationById(logFileRequest.applicationId),
            tag = logFileRequest.tag,
            logFormats = logFileRequest.logFormats,
            file = logFileRequest.file
        )
        val logsReceipt = logsService.processLogFile(logFileDTO)
        return LogsReceiptResponse(
            logsReceipt.application.id, logsReceipt.orderCounter, logsReceipt.logsCount, logsReceipt.source
        )
    }

    @ApiOperation("Load sample log data")
    @PostMapping("/sample")
    fun sampleData(authentication: Authentication): LogsReceiptResponse {
        val logSampleDTO = LogSampleDTO(
            user = userStorageService.findUserByEmail(authentication.name)
        )
        val logsReceipt = logsService.processLogSample(logSampleDTO)
        return LogsReceiptResponse(
            logsReceipt.application.id, logsReceipt.orderCounter, logsReceipt.logsCount, logsReceipt.source
        )
    }
}
