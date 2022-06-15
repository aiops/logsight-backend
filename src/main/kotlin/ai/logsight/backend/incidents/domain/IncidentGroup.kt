package ai.logsight.backend.incidents.domain

data class IncidentGroup(
    val head: Incident,
    val incidents: List<Incident>
)
