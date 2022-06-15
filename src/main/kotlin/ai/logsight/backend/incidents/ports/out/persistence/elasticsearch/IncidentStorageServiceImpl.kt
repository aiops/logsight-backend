package ai.logsight.backend.incidents.ports.out.persistence.elasticsearch

import ai.logsight.backend.charts.domain.dto.ChartConfig
import ai.logsight.backend.charts.domain.query.GetChartDataQuery
import ai.logsight.backend.charts.ports.web.request.ChartRequest
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.incidents.domain.Incident
import ai.logsight.backend.incidents.domain.service.command.DeleteIncidentCommand
import ai.logsight.backend.incidents.domain.service.command.UpdateIncidentCommand
import ai.logsight.backend.incidents.domain.service.query.FindIncidentByIdQuery
import ai.logsight.backend.incidents.domain.service.query.FindIncidentInTimeRangeQuery
import ai.logsight.backend.incidents.extensions.toIncidents
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESIncidents
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service

@Service
class IncidentStorageServiceImpl(
    private val elasticsearchService: ElasticsearchService,
    private val mapper: ObjectMapper
) : IncidentStorageService {

    override fun updateIncident(updateIncidentCommand: UpdateIncidentCommand): Incident {
        @Suppress("UNCHECKED_CAST")
        val parameters: Map<String, Any> = mapper.convertValue(
            updateIncidentCommand.incident, Map::class.java
        ) as Map<String, Any>

        // TODO check success on returned value
        elasticsearchService.updateFieldsByIndexAndDocID(
            parameters = parameters,
            updateIncidentCommand.index,
            updateIncidentCommand.incident.incidentId
        )

        return updateIncidentCommand.incident
    }

    override fun findIncidentById(findIncidentByIdQuery: FindIncidentByIdQuery): Incident {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "incidents_id",
                    "indexType" to "incidents",
                    "propertyId" to (findIncidentByIdQuery.incidentId)
                )
            )
        )
        // TODO fault tolerance in case no elements are found
        return queryIncidents(chartRequest, findIncidentByIdQuery.user)[0]
    }

    override fun findIncidentsInTimeRange(findIncidentInTimeRangeQuery: FindIncidentInTimeRangeQuery): List<Incident> {
        val chartRequest = ChartRequest(
            chartConfig = ChartConfig(
                mutableMapOf(
                    "type" to "util",
                    "feature" to "incidents_all",
                    "indexType" to "incidents",
                    "propertyId" to "",
                    "startTime" to findIncidentInTimeRangeQuery.rangeStart,
                    "stopTime" to findIncidentInTimeRangeQuery.rangeEnd
                )
            )
        )
        return queryIncidents(chartRequest, findIncidentInTimeRangeQuery.user)
    }

    override fun deleteIncidentById(deleteIncidentCommand: DeleteIncidentCommand): String {
        return elasticsearchService.deleteByIndexAndDocID(deleteIncidentCommand.index, deleteIncidentCommand.incidentId)
    }

    private fun queryIncidents(chartRequest: ChartRequest, user: User): List<Incident> {
        val getChartDataQuery = GetChartDataQuery(
            chartConfig = chartRequest.chartConfig,
            user = user,
        )
        val esIncidents = mapper.readValue<ESIncidents>(
            elasticsearchService.getData(
                getChartDataQuery, "${user.key}_${chartRequest.chartConfig.parameters["indexType"]}"
            )
        )
        return esIncidents.toIncidents()
    }
}
