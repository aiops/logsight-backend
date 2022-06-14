package ai.logsight.backend.incidents.domain.service.query

import ai.logsight.backend.users.domain.User

data class FindIncidentInTimeRangeQuery(
    val rangeStart: String,
    val rangeEnd: String,
    val user: User
)
