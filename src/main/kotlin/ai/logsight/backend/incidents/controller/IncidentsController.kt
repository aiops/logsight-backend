package ai.logsight.backend.compare.controller

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.response.CreateIncidentDataResponse
import ai.logsight.backend.incidents.controller.request.GetIncidentResultRequest
import ai.logsight.backend.incidents.service.IncidentService
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

@Api(tags = ["Incidents"], description = "Obtain incidents from log data")
@RestController
@RequestMapping("/api/v1/logs/incidents")
class IncidentsController(
    val incidentService: IncidentService,
    val userStorageService: UserStorageService
) {
    private val logger: LoggerImpl = LoggerImpl(IncidentsController::class.java)

    @ApiOperation("Obtain log incident results for specific application and time period.")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getIncidentResult(
        @Valid @RequestBody getIncidentResultRequest: GetIncidentResultRequest,
        authentication: Authentication
    ): CreateIncidentDataResponse {
        logger.info("Getting result data for incident with query parameters: $getIncidentResultRequest")
        val user = userStorageService.findUserByEmail(authentication.name)
        // Create charts command
        return incidentService.getIncidentResult(user, getIncidentResultRequest)
    }
}
