package ai.logsight.backend.charts.rest

import ai.logsight.backend.charts.ChartsService
import ai.logsight.backend.charts.domain.dto.Credentials
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.charts.rest.response.CreateChartResponse
import ai.logsight.backend.user.domain.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
class ChartsController(
    private val chartsService: ChartsService,
    private val userService: UserService
) {

    @GetMapping("/heatmap")
    @ResponseStatus(HttpStatus.CREATED)
    fun createHeatmap(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val user = userService.findByEmail(authentication.name)
        val query = GetChartDataQuery(
            credentials = Credentials(user.email, user.id.toString()),
            chartConfig = createChartRequest.chartConfig,
            dataSource = createChartRequest.dataSource,
            applicationId = createChartRequest.applicationId.toLong()
        )

        // Create charts command

        return CreateChartResponse(chartsService.createHeatMap(query))
    }

    @GetMapping("/barchart")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBarchart(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val user = userService.findByEmail(authentication.name)
        val query = GetChartDataQuery(
            credentials = Credentials(user.email, user.id.toString()),
            chartConfig = createChartRequest.chartConfig,
            dataSource = createChartRequest.dataSource,
            applicationId = createChartRequest.applicationId.toLong()
        )

        // Create charts command
        return CreateChartResponse(chartsService.createBarChart(query))
    }
}
