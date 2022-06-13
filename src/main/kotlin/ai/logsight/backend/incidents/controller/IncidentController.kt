package ai.logsight.backend.incidents.controller

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.incidents.controller.request.GetAllIncidentsRequest
import ai.logsight.backend.incidents.controller.request.UpdateIncidentStatusRequest
import ai.logsight.backend.incidents.controller.response.DeleteIncidentByIdResponse
import ai.logsight.backend.incidents.controller.response.GetAllIncidentResponse
import ai.logsight.backend.incidents.controller.response.GetIncidentByIdResponse
import ai.logsight.backend.incidents.controller.response.UpdateIncidentStatusResponse
import ai.logsight.backend.incidents.domain.IncidentViews
import ai.logsight.backend.incidents.service.IncidentService
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Incidents"], description = "Obtain incidents from log data")
@RestController
@RequestMapping("/api/v1/logs/incidents")
class IncidentController(
    val incidentService: IncidentService,
    val userStorageService: UserStorageService
) {
    private val logger: LoggerImpl = LoggerImpl(IncidentController::class.java)

    @ApiOperation("Get incident by ID")
    @GetMapping("/{incidentId}")
    @ResponseStatus(HttpStatus.OK)
    @JsonView(IncidentViews.Complete::class)
    fun getIncidentByID(
        authentication: Authentication,
        @PathVariable incidentId: String,
    ): GetIncidentByIdResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return GetIncidentByIdResponse(incidentService.getIncidentByID(incidentId, user))
    }

    @ApiOperation("Get all incidents for time interval")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    @JsonView(IncidentViews.Reduced::class)
    fun getAllIncidents(
        @Valid @RequestBody getAllIncidentsRequest: GetAllIncidentsRequest,
        authentication: Authentication
    ): GetAllIncidentResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return GetAllIncidentResponse(incidentService.getAllIncidents(user, getAllIncidentsRequest))
    }

    @ApiOperation("Delete incident by ID")
    @DeleteMapping("{incidentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteIncidentByID(
        authentication: Authentication,
        @PathVariable incidentId: String,
    ): DeleteIncidentByIdResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return DeleteIncidentByIdResponse(incidentService.deleteIncidentByID(incidentId, user))
    }

    @ApiOperation("Update incident status by ID")
    @PostMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    fun updateIncidentStatusByID(
        authentication: Authentication,
        @Valid @RequestBody updateIncidentStatusRequest: UpdateIncidentStatusRequest
    ): UpdateIncidentStatusResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return UpdateIncidentStatusResponse(
            incidentService.updateIncidentStatusByID(
                updateIncidentStatusRequest.incidentId, updateIncidentStatusRequest.incidentStatus, user
            )
        )
    }
}
