package ai.logsight.backend.application.ports.web.responses

import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationResponse

data class GetAllApplicationsResponse(
    val applications: List<ApplicationResponse>
)
