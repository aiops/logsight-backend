package ai.logsight.backend.application.ports.web.responses

data class GetAllApplicationsResponse(
    val applications: List<ApplicationResponse>
)
