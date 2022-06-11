package ai.logsight.backend.incidents.controller

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.ports.web.response.UpdateCompareStatusResponse
import ai.logsight.backend.incidents.controller.request.GetAllIncidentsRequest
import ai.logsight.backend.incidents.controller.request.UpdateIncidentStatusRequest
import ai.logsight.backend.incidents.controller.response.DeleteIncidentByIdResponse
import ai.logsight.backend.incidents.controller.response.GetAllIncidentResponse
import ai.logsight.backend.incidents.controller.response.GetIncidentByIdResponse
import ai.logsight.backend.incidents.service.IncidentService
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Incidents"], description = "Obtain incidents from log data")
@RestController
@RequestMapping("/api/v1/logs/incidents")
class IncidentsController(
    val incidentService: IncidentService, val userStorageService: UserStorageService
) {
    private val logger: LoggerImpl = LoggerImpl(IncidentsController::class.java)

    @ApiOperation("Get incident by ID")
    @GetMapping("/{incidentId}")
    @ResponseStatus(HttpStatus.OK)
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
    fun getAllIncidents(
        @Valid @RequestBody getAllIncidentsRequest: GetAllIncidentsRequest, authentication: Authentication
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
        authentication: Authentication, @Valid @RequestBody updateIncidentStatusRequest: UpdateIncidentStatusRequest
    ): UpdateCompareStatusResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        return UpdateCompareStatusResponse(
            incidentService.updateIncidentStatusByID(
                updateIncidentStatusRequest.incidentId, updateIncidentStatusRequest.incidentStatus, user
            )
        )
    }
}
