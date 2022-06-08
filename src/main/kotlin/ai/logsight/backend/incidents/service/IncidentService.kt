package ai.logsight.backend.incidents.service

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.TableChartData
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.compare.controller.response.CreateIncidentDataResponse
import ai.logsight.backend.compare.controller.response.IncidentResponse
import ai.logsight.backend.incidents.controller.request.GetIncidentResultRequest
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service

@Service
class IncidentService(
    private val chartsRepository: ESChartRepository,
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getIncidentResult(user: User, incidentQuery: GetIncidentResultRequest): CreateIncidentDataResponse {

        val getChartDataQuery = GetChartDataQuery(
            user = user,
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "tablechart",
                    "startTime" to incidentQuery.startTime,
                    "stopTime" to incidentQuery.stopTime,
                    "feature" to "incidents",
                    "indexType" to "incidents"
                )
            ),
            credentials = Credentials(user.email, user.key)
        )
        val incidentResultData =
            mapper.readValue<TableChartData>(chartsRepository.getData(getChartDataQuery, getChartDataQuery.chartConfig.parameters["indexType"] as String))
        val createGetIncidentResultResponse = CreateIncidentDataResponse(
            incidentResultData.hits.hits.map { incident ->
                IncidentResponse(
                    startTimestamp = incident.source.startTimestamp,
                    stopTimestamp = incident.source.stopTimestamp,
                    semanticThreats = incident.source.semanticAD,
                    totalScore = incident.source.totalScore
                )
            }
        )
        return createGetIncidentResultResponse
    }
}
