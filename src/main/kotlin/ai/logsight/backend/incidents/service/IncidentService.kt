package ai.logsight.backend.incidents.service

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.repository.ESChartRepository
import ai.logsight.backend.charts.repository.entities.elasticsearch.TableChartData
import ai.logsight.backend.common.dto.Credentials
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.common.utils.ApplicationIndicesBuilder
import ai.logsight.backend.compare.controller.response.CreateIncidentDataResponse
import ai.logsight.backend.compare.controller.response.IncidentResponse
import ai.logsight.backend.incidents.controller.request.GetIncidentResultRequest
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service

@Service
class IncidentService(
    private val applicationStorageService: ApplicationStorageService,
    private val chartsRepository: ESChartRepository,
    private val logsReceiptStorageService: LogsReceiptStorageService,
    private val applicationIndicesBuilder: ApplicationIndicesBuilder
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)

    fun getIncidentResult(incidentQuery: GetIncidentResultRequest): CreateIncidentDataResponse {
        val application = applicationStorageService.findApplicationById(incidentQuery.applicationId)
        val logsReceipt = incidentQuery.flushId?.let {
            logsReceiptStorageService.findLogsReceiptById(it)
        }
        // TODO check if logs with logs receipt ID are already in elsticsearch

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
                    applicationId = getChartDataQuery.application?.id,
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
