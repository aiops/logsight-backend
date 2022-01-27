package ai.logsight.backend.charts.rest

import ai.logsight.backend.charts.ChartsService
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.rest.request.ChartRequest
import ai.logsight.backend.charts.rest.response.CreateChartResponse
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/charts")
class ChartsController(
    private val chartsService: ChartsService,
    private val userService: UserService
) {

    @PostMapping("/heatmap")
    @ResponseStatus(HttpStatus.OK)
    fun createHeatmap(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        val query = GetChartDataQuery(
            credentials = Credentials(user.email, user.id.toString()),
            chartConfig = createChartRequest.chartConfig,
            dataSource = createChartRequest.dataSource,
            applicationId = createChartRequest.applicationId.toLong()
        )
        println(query)

        // Create charts command

        return CreateChartResponse(chartsService.createHeatMap(query))
    }

    @GetMapping("/barchart")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBarchart(
        authentication: Authentication,
        @RequestBody createChartRequest: ChartRequest
    ): CreateChartResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        val query = GetChartDataQuery(
            credentials = Credentials(user.email, user.id.toString()),
            chartConfig = createChartRequest.chartConfig,
            dataSource = createChartRequest.dataSource,
            applicationId = createChartRequest.applicationId
        )

        // Create charts command
        return CreateChartResponse(chartsService.createBarChart(query))
    }
}
