package ai.logsight.backend.charts.ports.web

import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.charts.ports.web.response.CreateChartResponse
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.ports.web.request.GetCompareAnalyticsIssueKPIRequest
import ai.logsight.backend.compare.ports.web.response.CompareAnalyticsIssueKPIResponse
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

@ApiIgnore
@RestController
@RequestMapping("/api/v1/users/{userId}/charts")
class ChartsController(
    private val chartsService: ChartsService,
) {

    private val logger = LoggerImpl(ChartsController::class.java)

    @PostMapping("/barchart")
    @ResponseStatus(HttpStatus.OK)
    fun createBarchart(
        @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "userId must be UUID type."
        ) @NotEmpty(message = "userId must not be empty.") userId: String,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.debug("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(UUID.fromString(userId), createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createBarChart(query))
    }

    @PostMapping("/piechart")
    @ResponseStatus(HttpStatus.OK)
    fun createPieChart(
        @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "userId must be UUID type."
        ) @NotEmpty(message = "userId must not be empty.") userId: String,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.debug("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(UUID.fromString(userId), createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createPieChart(query))
    }

    @PostMapping("/tablechart")
    @ResponseStatus(HttpStatus.OK)
    fun createTableChart(
        @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "userId must be UUID type."
        ) @NotEmpty(message = "userId must not be empty.") userId: String,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.debug("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(UUID.fromString(userId), createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createTableChart(query))
    }

    @ApiOperation("Get analytics KPI for issues")
    @PostMapping("/map")
    @ResponseStatus(HttpStatus.OK)
    fun getAnalyticsIssuesKPI(
        @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "userId must be UUID type."
        ) @NotEmpty(message = "userId must not be empty.") userId: String,
        @Valid @RequestBody getCompareAnalyticsIssueKPIRequest: GetCompareAnalyticsIssueKPIRequest
    ): CompareAnalyticsIssueKPIResponse {
        val result = chartsService.getAnalyticsIssuesKPI(
            UUID.fromString(userId),
            getCompareAnalyticsIssueKPIRequest.baselineTags
        )
        return CompareAnalyticsIssueKPIResponse(result)
    }
}
