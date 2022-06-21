package ai.logsight.backend.incidents.domain.service.command

import ai.logsight.backend.incidents.domain.Incident

data class UpdateIncidentCommand(
    val incident: Incident,
    val index: String
)
