package ai.logsight.backend.autolog.ports.web

import ai.logsight.backend.autolog.domain.dto.AutoLogDTO
import ai.logsight.backend.autolog.ports.web.response.AutoLogResponse
import ai.logsight.backend.autolog.domain.service.AutoLogService
import ai.logsight.backend.autolog.ports.web.out.persistance.AutoLogEntity
import ai.logsight.backend.autolog.ports.web.request.AutoLogFeedbackRequest
import ai.logsight.backend.autolog.ports.web.request.AutoLogRequest
import ai.logsight.backend.autolog.ports.web.response.AutoLogEntry
import ai.logsight.backend.autolog.ports.web.response.AutoLogFeedbackResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["AutoLog"], description = "Generate log location for a given code file.")
@RestController
@RequestMapping("/api/v1/logs/autolog")
class AutoLogController(
    val autoLogService: AutoLogService,
    val userStorageService: UserStorageService
    ) {

    @ApiOperation("Request log locations for specific code file.")
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun getAutoLogRecommendations(
        authentication: Authentication,
        @Valid @RequestBody autoLogRequest: AutoLogRequest,
    ): AutoLogResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return autoLogService.getAutoLogs(AutoLogDTO(user, autoLogRequest.context, autoLogRequest.fileName, autoLogRequest.languageId, autoLogRequest.source))
    }

    @ApiOperation("Give feedback about the autolog recommendation")
    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    fun giveFeedback(
        authentication: Authentication,
        @Valid @RequestBody autoLogFeedbackRequest: AutoLogFeedbackRequest,
    ): AutoLogFeedbackResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        autoLogService.giveFeedback(user, autoLogFeedbackRequest.autoLogId, autoLogFeedbackRequest.isHelpful)
        return AutoLogFeedbackResponse()
    }

}
