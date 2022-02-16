package ai.logsight.backend.charts.rest

import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.charts.rest.response.CreateChartResponse
import ai.logsight.backend.common.logging.LoggerImpl
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

    @PostMapping("/heatmap")
    @ResponseStatus(HttpStatus.OK)
    fun createHeatmap(
        @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "userId must be UUID type."
        ) @NotEmpty(message = "userId must not be empty.") userId: String,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(UUID.fromString(userId), createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createHeatMap(query))
    }

    @PostMapping("/barchart")
    @ResponseStatus(HttpStatus.OK)
    fun createBarchart(
        @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "userId must be UUID type."
        ) @NotEmpty(message = "userId must not be empty.") userId: String,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
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
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
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
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(UUID.fromString(userId), createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createTableChart(query))
    }
}
