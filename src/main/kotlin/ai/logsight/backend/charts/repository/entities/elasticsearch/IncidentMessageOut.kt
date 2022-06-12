package ai.logsight.backend.charts.repository.entities.elasticsearch

data class IncidentMessageOut(
    val timestamp: String,
    val template: String,
    val level: String,
    val riskScore: Double,
    val message: String,
    val tags: Map<String, String>,
    val addedState: Long,
    val prediction: Long,
    val riskSeverity: Long,
    val tagString: String,
)
