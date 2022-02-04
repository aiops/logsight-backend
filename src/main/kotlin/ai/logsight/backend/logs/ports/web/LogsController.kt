package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.logs.domain.LogFormat
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.domain.service.dto.LogBatchDTO
import ai.logsight.backend.logs.domain.service.dto.LogFileDTO
import ai.logsight.backend.logs.domain.service.dto.LogSampleDTO
import ai.logsight.backend.logs.ports.web.requests.SendLogFileRequest
import ai.logsight.backend.logs.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ports.web.responses.SampleDataResponse
import ai.logsight.backend.logs.ports.web.responses.SendFileResponse
import ai.logsight.backend.logs.ports.web.responses.SendLogsResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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
        @Valid @RequestBody logListRequest: SendLogListRequest
    ): SendLogsResponse {
        val logBatchDTO = LogBatchDTO(
            userEmail = authentication.name,
            applicationId = logListRequest.applicationId,
            tag = logListRequest.tag,
            logFormat = logListRequest.logFormat,
            logs = logListRequest.logs
        )
        logsService.forwardLogs(logBatchDTO)
        // TODO("Alex needs to have a look at this about the Flush")
        return SendLogsResponse(
            description = "Log batch received successfully",
            applicationId = logListRequest.applicationId,
            tag = logListRequest.tag
        )
    }

    @PostMapping("/file")
    fun uploadFile(
        authentication: Authentication,
        @RequestBody logFileRequest: SendLogFileRequest
    ): SendFileResponse {
        val logFileDTO = LogFileDTO(
            userEmail = authentication.name,
            applicationName = logFileRequest.applicationName,
            tag = logFileRequest.tag,
            logFormat = logFileRequest.logFormat,
            file = logFileRequest.file
        )
        val application = logsService.processFile(logFileDTO)
        // TODO("Alex needs to have a look at this about the Flush")
        return SendFileResponse("File upload was successful.")
    }

    @PostMapping("/sample")
    fun sampleData(
        authentication: Authentication
    ): SampleDataResponse {
        val logSampleDTO = LogSampleDTO (
            userEmail = authentication.name
        )
        logsService.uploadSampleData(logSampleDTO)
        // TODO("Alex needs to have a look at this about the Flush")
        return SampleDataResponse("Sample data was loaded successfully.")
    }
}
