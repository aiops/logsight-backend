package ai.logsight.backend.compare.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsDataPoint
import ai.logsight.backend.charts.repository.entities.elasticsearch.TableChartData
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.ApplicationIndicesBuilder
import ai.logsight.backend.compare.controller.request.GetIncidentResultRequest
import ai.logsight.backend.compare.controller.response.CreateIncidentDataResponse
import ai.logsight.backend.compare.controller.response.IncidentResponse
import ai.logsight.backend.compare.out.rest.config.CompareRESTConfigProperties
import ai.logsight.backend.connectors.rest.RestTemplateConnector
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.exceptions.ResultInitAlreadyPendingException
import ai.logsight.backend.results.ports.persistence.ResultInitStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.net.http.HttpClient

@Service
class IncidentService(
    private val restConfigProperties: CompareRESTConfigProperties,
    private val applicationStorageService: ApplicationStorageService,
    private val resultInitStorageService: ResultInitStorageService,
    private val chartsRepository: ESChartRepository,
    private val applicationIndicesBuilder: ApplicationIndicesBuilder
) {
    val resetTemplate = RestTemplateConnector()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getIncidentResult(incidentQuery: GetIncidentResultRequest): CreateIncidentDataResponse {
        val application = applicationStorageService.findApplicationById(incidentQuery.applicationId)
        val resultInit = incidentQuery.flushId?.let {
            resultInitStorageService.findResultInitById(it)
        }
        if (resultInit != null && resultInit.status != ResultInitStatus.DONE) {
            throw ResultInitAlreadyPendingException("Result init is not yet ready. Please try again later, initiate new result, or send a request without an ID to force getting results.")
        }
        val getChartDataQuery = GetChartDataQuery(
            application = application, user = application.user,
            chartConfig = ChartConfig(
                "tablechart",
                incidentQuery.startTime,
                incidentQuery.stopTime,
                "incidents",
                "incidents"
            ),
            credentials = Credentials(application.user.email, application.user.key)
        )
        val applicationIndices = applicationIndicesBuilder.buildIndices(
            getChartDataQuery.user,
            getChartDataQuery.application,
            getChartDataQuery.chartConfig.indexType
        )
        val incidentResultData =
            mapper.readValue<TableChartData>(chartsRepository.getData(getChartDataQuery, applicationIndices))
        val createGetIncidentResultResponse = CreateIncidentDataResponse(
            incidentResultData.hits.hits.map { incident ->
                IncidentResponse(
                    applicationId = getChartDataQuery.application?.id, startTimestamp = incident.source.startTimestamp, stopTimestamp = incident.source.stopTimestamp, semanticThreats = incident.source.semanticAD, totalScore = incident.source.totalScore
                )
            }
        )
        return createGetIncidentResultResponse
    }
}
