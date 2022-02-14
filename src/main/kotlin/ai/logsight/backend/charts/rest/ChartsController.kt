package ai.logsight.backend.charts.rest

import ai.logsight.backend.charts.domain.service.ChartsService
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.charts.rest.response.CreateChartResponse
import ai.logsight.backend.common.logging.LoggerImpl
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import javax.validation.Valid

@ApiIgnore
@RestController
@RequestMapping("/api/v1/charts")
class ChartsController(
    private val chartsService: ChartsService,
) {

    private val logger = LoggerImpl(ChartsController::class.java)

    @PostMapping("/heatmap")
    @ResponseStatus(HttpStatus.OK)
    fun createHeatmap(
        authentication: Authentication,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createHeatMap(query))
    }

    @PostMapping("/barchart")
    @ResponseStatus(HttpStatus.OK)
    fun createBarchart(
        authentication: Authentication,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createBarChart(query))
    }

    @PostMapping("/piechart")
    @ResponseStatus(HttpStatus.OK)
    fun createPieChart(
        authentication: Authentication,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createPieChart(query))
    }

    @PostMapping("/tablechart")
    @ResponseStatus(HttpStatus.OK)
    fun createTableChart(
        authentication: Authentication,
        @Valid @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        logger.info("Getting chart data with query parameters: ${createChartRequest.chartConfig}")
        val query = chartsService.getChartQuery(authentication, createChartRequest)
        // Create charts command
        return CreateChartResponse(chartsService.createTableChart(query))
    }
}
