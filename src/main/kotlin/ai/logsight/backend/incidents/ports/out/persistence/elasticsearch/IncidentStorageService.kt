package ai.logsight.backend.incidents.ports.out.persistence.elasticsearch

import ai.logsight.backend.incidents.domain.Incident
import ai.logsight.backend.incidents.domain.service.command.DeleteIncidentCommand
import ai.logsight.backend.incidents.domain.service.command.UpdateIncidentCommand
import ai.logsight.backend.incidents.domain.service.query.FindIncidentByIdQuery
import ai.logsight.backend.incidents.domain.service.query.FindIncidentInTimeRangeQuery

interface IncidentStorageService {
    fun updateIncident(updateIncidentCommand: UpdateIncidentCommand): Incident

    fun findIncidentById(findIncidentByIdQuery: FindIncidentByIdQuery): Incident

    fun findIncidentsInTimeRange(findIncidentInTimeRangeQuery: FindIncidentInTimeRangeQuery): List<Incident>

    fun deleteIncidentById(deleteIncidentCommand: DeleteIncidentCommand): String
}
