package ai.logsight.backend.incidents.domain

data class Incident(
    val incidentId: String,
    val timestamp: String,
    val risk: Long,
    val countMessages: Long,
    val countStates: Long,
    val status: Long,
    val countAddedState: Long,
    val countLevelFault: Long,
    val severity: Long,
    val tags: Map<String, String>,
    val countSemanticAnomaly: Long,
    val message: IncidentMessage,
    val data: List<IncidentMessage>
)
