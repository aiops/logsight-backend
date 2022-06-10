package ai.logsight.backend.incidents.service

import ai.logsight.backend.charts.domain.service.ESChartsServiceImpl
import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsIncidentAllDataPoint
import ai.logsight.backend.charts.repository.entities.elasticsearch.HitsIncidentDataPoint
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.incidents.controller.request.GetAllIncidentsRequest
import ai.logsight.backend.users.domain.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Service

@Service
class IncidentService(
    private val esChartsServiceImpl: ESChartsServiceImpl,
    private val elasticsearchService: ElasticsearchService,
) {
    val mapper = ObjectMapper().registerModule(KotlinModule())!!

    private val logger = LoggerImpl(ChartsController::class.java)


    fun getIncidentByID(incidentId: String?, user: User): List<HitsIncidentDataPoint> {
        return esChartsServiceImpl.getIncidentByID(incidentId, user)
    }

    fun getAllIncidents(user: User, getAllIncidentsRequest: GetAllIncidentsRequest): List<HitsIncidentAllDataPoint> {
        return esChartsServiceImpl.getAllIncidents(user, getAllIncidentsRequest)
    }

    fun updateIncidentStatusByID(incidentId: String, incidentStatus: Long, user: User): String {
        val parameters = hashMapOf("status" to (incidentStatus as Any))
        return elasticsearchService.updateFieldByIndexAndDocID(parameters, "${user.key}_incidents", incidentId)
    }

    fun deleteIncidentByID(incidentId: String, user: User): String {
        return elasticsearchService.deleteByIndexAndDocID("${user.key}_incidents", incidentId)
    }
}
