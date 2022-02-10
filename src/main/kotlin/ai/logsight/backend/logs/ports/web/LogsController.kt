package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO
import ai.logsight.backend.logs.ports.web.requests.SendLogFileRequest
import ai.logsight.backend.logs.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ports.web.responses.LogsReceiptResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Logs"], description = " ")
@RestController
@RequestMapping("/api/v1/logs")
class LogsController(
    val logsService: LogsService,
) {

    @ApiOperation("Send list of log messages for analysis")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun sendLogList(
        authentication: Authentication,
        @Valid @RequestBody logListRequest: SendLogListRequest
    ): LogsReceiptResponse {
        val logBatchDTO = LogBatchDTO(
            userEmail = authentication.name,
            applicationId = logListRequest.applicationId,
            tag = logListRequest.tag,
            logFormat = logListRequest.logFormat,
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
            userEmail = authentication.name,
            applicationId = logFileRequest.applicationId,
            tag = logFileRequest.tag,
            logFormat = logFileRequest.logFormat,
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
            userEmail = authentication.name
        )
        val logsReceipt = logsService.processLogSample(logSampleDTO)
        return LogsReceiptResponse(
            logsReceipt.application.id, logsReceipt.orderCounter, logsReceipt.logsCount, logsReceipt.source
        )
    }
}
