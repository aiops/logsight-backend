package ai.logsight.backend.incidents.domain.service.command

data class DeleteIncidentCommand(
    val incidentId: String,
    val index: String
)
