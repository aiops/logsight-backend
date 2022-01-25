package ai.logsight.backend.charts.rest

import ai.logsight.backend.charts.ChartsService
import ai.logsight.backend.charts.domain.charts.query.GetChartDataQuery
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.charts.rest.response.CreateChartResponse
import ai.logsight.backend.user.domain.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
class ChartsController(
    private val chartsService: ChartsService,
    private val userService: UserService
) {

    @GetMapping("/heatmap")
    @ResponseStatus(HttpStatus.CREATED)
    fun createChart(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val user = userService.findByEmail(authentication.name)
        val query = GetChartDataQuery(
            username = user.email, password = user.id.toString(),
            chartType = "heatmap",
            startTime = createChartRequest.chartConfig.startTime,
            stopTime = createChartRequest.chartConfig.stopTime,
            index = createChartRequest.dataSource.index,
            applicationId = createChartRequest.applicationId,
            feature = createChartRequest.feature
        )
        chartsService.createHeatmap(query)
        // Create charts command

        return CreateChartResponse()
    }
}
