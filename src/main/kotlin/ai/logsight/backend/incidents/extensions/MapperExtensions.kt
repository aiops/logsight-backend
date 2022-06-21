package ai.logsight.backend.incidents.extensions

import ai.logsight.backend.incidents.domain.Incident
import ai.logsight.backend.incidents.domain.IncidentGroup
import ai.logsight.backend.incidents.domain.IncidentMessage
import ai.logsight.backend.incidents.domain.dto.IncidentDTO
import ai.logsight.backend.incidents.domain.dto.IncidentGroupDTO
import ai.logsight.backend.incidents.domain.dto.IncidentMessageDTO
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESHitsIncident
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESIncident
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESIncidentMessage
import ai.logsight.backend.incidents.ports.out.persistence.elasticsearch.entities.ESIncidents

fun ESIncidentMessage.toIncidentMessage() =
    IncidentMessage(
        timestamp = timestamp,
        template = template,
        level = level,
        riskScore = riskScore,
        message = message,
        tags = tags,
        addedState = addedState,
        prediction = prediction,
        riskSeverity = riskSeverity,
        tagString = tagString
    )

fun ESHitsIncident.toIncident() =
    Incident(
        incidentId = incidentId,
        timestamp = source.timestamp,
        risk = source.risk,
        countMessages = source.countMessages,
        countStates = source.countStates,
        status = source.status,
        countAddedState = source.countAddedState,
        countLevelFault = source.countLevelFault,
        severity = source.severity,
        tags = source.tags,
        countSemanticAnomaly = source.countSemanticAnomaly,
        message = source.data.maxByOrNull { it.riskScore }!!.toIncidentMessage(),
        data = source.data.map { it.toIncidentMessage() }
    )

fun ESIncidents.toIncidents() =
    this.hits.hits.map { esIncident ->
        esIncident.toIncident()
    }

fun IncidentMessage.toESIncidentMessage() =
    ESIncidentMessage(
        timestamp = timestamp,
        template = template,
        level = level,
        riskScore = riskScore,
        message = message,
        tags = tags,
        addedState = addedState,
        prediction = prediction,
        riskSeverity = riskSeverity,
        tagString = tagString
    )

fun IncidentMessage.toIncidentMessageDTO() =
    IncidentMessageDTO(
        timestamp = timestamp,
        template = template,
        level = level,
        riskScore = riskScore,
        message = message,
        tags = tags,
        addedState = addedState,
        prediction = prediction,
        riskSeverity = riskSeverity,
        tagString = tagString
    )

fun IncidentMessageDTO.toIncidentMessage() =
    IncidentMessage(
        timestamp = timestamp,
        template = template,
        level = level,
        riskScore = riskScore,
        message = message,
        tags = tags,
        addedState = addedState,
        prediction = prediction,
        riskSeverity = riskSeverity,
        tagString = tagString
    )

fun Incident.toESIncident() =
    ESIncident(
        timestamp = timestamp,
        risk = risk,
        countMessages = countMessages,
        countStates = countStates,
        status = status,
        countAddedState = countAddedState,
        countLevelFault = countLevelFault,
        severity = severity,
        tags = tags,
        countSemanticAnomaly = countSemanticAnomaly,
        data = data.map { it.toESIncidentMessage() }
    )

fun Incident.toIncidentDTO() =
    IncidentDTO(
        incidentId = incidentId,
        timestamp = timestamp,
        risk = risk,
        countMessages = countMessages,
        countStates = countStates,
        status = status,
        countAddedState = countAddedState,
        countLevelFault = countLevelFault,
        severity = severity,
        tags = tags,
        countSemanticAnomaly = countSemanticAnomaly,
        message = message.toIncidentMessageDTO(),
        data = data.map { it.toIncidentMessageDTO() }
    )

fun IncidentDTO.toIncident() =
    Incident(
        incidentId = incidentId,
        timestamp = timestamp,
        risk = risk,
        countMessages = countMessages,
        countStates = countStates,
        status = status,
        countAddedState = countAddedState,
        countLevelFault = countLevelFault,
        severity = severity,
        tags = tags,
        countSemanticAnomaly = countSemanticAnomaly,
        message = message.toIncidentMessage(),
        data = data?.map { it.toIncidentMessage() } ?: emptyList()
    )

fun IncidentGroup.toIncidentGroupDTO() =
    IncidentGroupDTO(
        head = head.toIncidentDTO(),
        incidents = incidents.map { it.toIncidentDTO() }
    )
