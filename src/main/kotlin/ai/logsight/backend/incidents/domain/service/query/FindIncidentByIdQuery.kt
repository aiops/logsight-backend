package ai.logsight.backend.incidents.domain.service.query

import ai.logsight.backend.users.domain.User

data class FindIncidentByIdQuery(
    val incidentId: String,
    val user: User
)
