package ai.logsight.backend.charts.repository.entities.elasticsearch.incidents

import ai.logsight.backend.incidents.domain.Incident

fun ESIncidents.toIncidents() =
    this.hits.hits.map { esIncident ->
        Incident(
            incidentId = esIncident.incidentId,
            timestamp = esIncident.source.timestamp,
            risk = esIncident.source.risk,
            countMessages = esIncident.source.countMessages,
            countStates = esIncident.source.countStates,
            status = esIncident.source.status,
            countAddedState = esIncident.source.countAddedState,
            countLevelFault = esIncident.source.countLevelFault,
            severity = esIncident.source.severity,
            tags = esIncident.source.tags,
            countSemanticAnomaly = esIncident.source.countSemanticAnomaly,
            message = esIncident.source.data.maxByOrNull { it.riskScore }!!,
            data = esIncident.source.data
        )
    }
