package ai.logsight.backend.incidents.ports.web

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.incidents.domain.dto.IncidentDTOViews
import ai.logsight.backend.incidents.domain.service.IncidentService
import ai.logsight.backend.incidents.extensions.toIncident
import ai.logsight.backend.incidents.extensions.toIncidentDTO
import ai.logsight.backend.incidents.extensions.toIncidentGroupDTO
import ai.logsight.backend.incidents.ports.web.request.GetGroupedIncidentsRequest
import ai.logsight.backend.incidents.ports.web.request.GetIncidentsRequest
import ai.logsight.backend.incidents.ports.web.request.UpdateIncidentRequest
import ai.logsight.backend.incidents.ports.web.response.*
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

    @ApiOperation("Get all incidents for time interval")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    @JsonView(IncidentDTOViews.Reduced::class)
    fun getIncidentsInTimeRange(
        @Valid @RequestBody getIncidentsRequest: GetIncidentsRequest,
        authentication: Authentication
    ): GetIncidentsResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val incidents = incidentService.getIncidentsInTimeRange(
            getIncidentsRequest.startTime, getIncidentsRequest.stopTime, user
        )
        return GetIncidentsResponse(incidents.map { it.toIncidentDTO() })
    }

    @ApiOperation("Get all grouped incidents for time interval")
    @PostMapping("/grouped")
    @ResponseStatus(HttpStatus.OK)
    @JsonView(IncidentDTOViews.Reduced::class)
    fun getGroupedIncidentsInTimeRange(
        @Valid @RequestBody getGroupedIncidentsRequest: GetGroupedIncidentsRequest,
        authentication: Authentication
    ): GetGroupedIncidentsResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val incidentGroups = incidentService.getGroupedIncidentsInTimeRange(
            getGroupedIncidentsRequest.startTime, getGroupedIncidentsRequest.stopTime, user
        )
        return GetGroupedIncidentsResponse(incidentGroups.map { it.toIncidentGroupDTO() })
    }

    @ApiOperation("Get incident by ID")
    @GetMapping("/{incidentId}")
    @ResponseStatus(HttpStatus.OK)
    @JsonView(IncidentDTOViews.Complete::class)
    fun getIncidentByID(
        authentication: Authentication,
        @PathVariable incidentId: String,
    ): GetIncidentByIdResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val incident = incidentService.getIncidentByID(incidentId, user)
        return GetIncidentByIdResponse(incident = incident.toIncidentDTO())
    }

    @ApiOperation("Delete incident by ID")
    @DeleteMapping("{incidentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteIncidentByID(
        authentication: Authentication,
        @PathVariable incidentId: String,
    ): DeleteIncidentByIdResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val deletedIncidentId = incidentService.deleteIncidentByID(incidentId, user)
        return DeleteIncidentByIdResponse(deletedIncidentId)
    }

    @ApiOperation("Update incident by ID")
    @PutMapping("/{incidentId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateIncidentStatusByID(
        authentication: Authentication,
        @PathVariable incidentId: String,
        @Valid @RequestBody updateIncidentRequest: UpdateIncidentRequest
    ): UpdateIncidentResponse {
        val user = userStorageService.findUserByEmail(authentication.name)
        val incident = incidentService.updateIncident(updateIncidentRequest.incident.toIncident(), user)
        return UpdateIncidentResponse(incident.toIncidentDTO())
    }
}
