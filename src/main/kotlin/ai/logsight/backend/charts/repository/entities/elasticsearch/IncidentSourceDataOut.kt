package ai.logsight.backend.charts.repository.entities.elasticsearch

data class IncidentSourceDataOut(
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
    val message: IncidentMessageOut,
    val data: List<IncidentMessageOut>
)
