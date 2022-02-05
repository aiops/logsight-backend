package ai.logsight.backend.application.ports.web.responses

import ai.logsight.backend.application.domain.Application

data class GetAllApplicationsResponse(
    val applications: List<Application>
)
