package ai.logsight.backend.incidents.domain.service

import ai.logsight.backend.charts.ports.web.ChartsController
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.incidents.domain.Incident
import ai.logsight.backend.incidents.domain.service.command.DeleteIncidentCommand
import ai.logsight.backend.incidents.domain.service.command.UpdateIncidentCommand
import ai.logsight.backend.incidents.domain.service.query.FindIncidentByIdQuery
import ai.logsight.backend.incidents.domain.service.query.FindIncidentInTimeRangeQuery
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.IncidentStorageService
import ai.logsight.backend.users.domain.User
import org.springframework.stereotype.Service

@Service
class IncidentService(
    private val incidentStorageService: IncidentStorageService
) {
    private val logger = LoggerImpl(ChartsController::class.java)

    fun getIncidentByID(incidentId: String, user: User): Incident {
        val findIncidentByIdQuery = FindIncidentByIdQuery(incidentId, user)
        return incidentStorageService.findIncidentById(findIncidentByIdQuery)
    }

    fun getIncidentsInTimeRange(rangeStart: String, rangeEnd: String, user: User): List<Incident> {
        val findIncidentInTimeRangeQuery = FindIncidentInTimeRangeQuery(rangeStart, rangeEnd, user)
        return incidentStorageService.findIncidentsInTimeRange(findIncidentInTimeRangeQuery)
    }

    fun updateIncident(incident: Incident, user: User): Incident {
        val currentIncident = getIncidentByID(incident.incidentId, user)
        val updatedIncident = currentIncident.copy(
            incidentId = incident.incidentId,
            timestamp = incident.timestamp,
            risk = incident.risk,
            countMessages = incident.countMessages,
            countStates = incident.countStates,
            status = incident.status,
            countAddedState = incident.countAddedState,
            countLevelFault = incident.countLevelFault,
            severity = incident.severity,
            tags = incident.tags,
            countSemanticAnomaly = incident.countSemanticAnomaly,
            message = incident.message,
        )
        val updateIncidentCommand = UpdateIncidentCommand(updatedIncident, getIncidentIndex(user))
        return incidentStorageService.updateIncident(updateIncidentCommand)
    }

    fun deleteIncidentByID(incidentId: String, user: User): String {
        val deleteIncidentCommand = DeleteIncidentCommand(incidentId, getIncidentIndex(user))
        return incidentStorageService.deleteIncidentById(deleteIncidentCommand)
    }

    private fun getIncidentIndex(user: User): String = "${user.key}_incidents"
}
