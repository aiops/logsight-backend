package ai.logsight.backend.logwriter.ports.web

import ai.logsight.backend.logwriter.domain.dto.LogWriterDTO
import ai.logsight.backend.logwriter.ports.web.response.LogWriterResponse
import ai.logsight.backend.logwriter.domain.service.LogWriterService
import ai.logsight.backend.logwriter.ports.web.request.LogWriterFeedbackRequest
import ai.logsight.backend.logwriter.ports.web.request.LogWriterRequest
import ai.logsight.backend.logwriter.ports.web.response.LogWriterFeedbackResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Log writer"], description = "Generate logs for a code file.")
@RestController
@RequestMapping("/api/v1/logs/writer")
class LogWriterController(
    val logWriterService: LogWriterService,
    val userStorageService: UserStorageService
    ) {

    @ApiOperation("Request log locations for specific code file.")
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun getLogWriterRecommendations(
        authentication: Authentication,
        @Valid @RequestBody logWriterRequest: LogWriterRequest,
    ): LogWriterResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return logWriterService.getLogWriterLogs(LogWriterDTO(user, logWriterRequest.code, logWriterRequest.language))
    }

    @ApiOperation("Give feedback about the log writer recommendation")
    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    fun giveFeedback(
        authentication: Authentication,
        @Valid @RequestBody logWriterFeedbackRequest: LogWriterFeedbackRequest,
    ): LogWriterFeedbackResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        logWriterService.giveFeedback(user, logWriterFeedbackRequest.autoLogId, logWriterFeedbackRequest.isHelpful)
        return LogWriterFeedbackResponse()
    }

}
