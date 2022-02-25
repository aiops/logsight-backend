package ai.logsight.backend.compare.controller

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.ports.web.response.CreateChartResponse
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.request.GetIncidentResultRequest
import ai.logsight.backend.compare.controller.response.CreateIncidentDataResponse
import ai.logsight.backend.compare.service.IncidentService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Incidents"], description = "Obtain incidents from log data")
@RestController
@RequestMapping("/api/v1/logs/incidents")
class IncidentsController(
    val incidentService: IncidentService,
    val applicationStorageService: ApplicationStorageService,
) {
    private val logger: LoggerImpl = LoggerImpl(IncidentsController::class.java)

    @ApiOperation("Obtain log incident results for specific application and time period.")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getIncidentResult(@Valid @RequestBody getIncidentResultRequest: GetIncidentResultRequest): CreateIncidentDataResponse {
        logger.info("Getting result data for incident with query parameters: $getIncidentResultRequest")
        // Create charts command
        return CreateIncidentDataResponse(incidentService.getIncidentResult(getIncidentResultRequest))
    }
}
