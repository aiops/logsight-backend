package ai.logsight.backend.logs.ports.web

import ai.logsight.backend.logs.domain.LogFileTypes
import ai.logsight.backend.logs.domain.service.LogsService
import ai.logsight.backend.logs.domain.service.command.LogCommand
import ai.logsight.backend.logs.ports.web.requests.SendFileRequest
import ai.logsight.backend.logs.ports.web.requests.SendLogListRequest
import ai.logsight.backend.logs.ports.web.responses.SampleDataResponse
import ai.logsight.backend.logs.ports.web.responses.SendFileResponse
import ai.logsight.backend.logs.ports.web.responses.SendLogsResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        @Valid @RequestBody logRequest: SendLogListRequest
    ): SendLogsResponse {

        val logCommand = LogCommand(
            userEmail = authentication.name,
            applicationId = logRequest.applicationId,
            tag = logRequest.tag,
            logFormat = LogFileTypes.UNKNOWN_FORMAT,
            logs = logRequest.logs
        )
        logsService.forwardLogs(logCommand)
        return SendLogsResponse(
            description = "Log batch received successfully",
            applicationId = logRequest.applicationId,
            tag = logRequest.tag
        )
    }

    @PostMapping("/upload_file")
    fun uploadFile(
        authentication: Authentication,
        @RequestBody fileRequest: SendFileRequest
    ): SendFileResponse {
        val application = logsService.processFile(fileRequest, authentication.name)
        return SendFileResponse("File upload was successful.", applicationId = application.id)
    }

    @PostMapping("/sample_data")
    fun sampleData(
        authentication: Authentication
    ): SampleDataResponse {
        // Internal routine to upload data
        logsService.uploadSampleData(authentication.name)
        return SampleDataResponse("Sample data was loaded successfully.")
    }
}
