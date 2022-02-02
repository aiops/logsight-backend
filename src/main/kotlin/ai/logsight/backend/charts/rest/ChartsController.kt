package ai.logsight.backend.charts.rest

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.ChartsService
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.charts.rest.response.CreateChartResponse
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.timeselection.domain.service.TimeSelectionService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/charts")
class ChartsController(
    private val chartsService: ChartsService,
    private val userService: TimeSelectionService,
    private val applicationService: ApplicationStorageService
) {

    @PostMapping("/heatmap")
    @ResponseStatus(HttpStatus.OK)
    fun createHeatmap(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val query = getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createHeatMap(query))
    }

    @GetMapping("/barchart")
    @ResponseStatus(HttpStatus.OK)
    fun createBarchart(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val query = getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createBarChart(query))
    }

    @GetMapping("/piechart")
    @ResponseStatus(HttpStatus.OK)
    fun createPieChart(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val query = getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createPieChart(query))
    }

    @GetMapping("/tablechart")
    @ResponseStatus(HttpStatus.OK)
    fun createTableChart(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val query = getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createTableChart(query))
    }

    fun getChartQuery(authentication: Authentication, createChartRequest: ChartRequest): GetChartDataQuery {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        val application =
            createChartRequest.applicationId?.let { applicationService.findApplicationById(createChartRequest.applicationId) }
        return GetChartDataQuery(
            credentials = Credentials(user.email, user.id.toString()),
            chartConfig = createChartRequest.chartConfig,
            user = user,
            application = application
        )
    }
}
