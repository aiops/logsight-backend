package ai.logsight.backend.incidents.controller.request

import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class UpdateIncidentStatusRequest(
    val incidentId: String,
    @get:Min(1)
    @get:Max(3)
    val incidentStatus: Long
)
