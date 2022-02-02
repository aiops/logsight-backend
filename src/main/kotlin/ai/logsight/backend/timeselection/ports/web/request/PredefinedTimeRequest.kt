package ai.logsight.backend.timeselection.ports.web.request

import ai.logsight.backend.timeselection.ports.out.persistence.DateTimeType

data class PredefinedTimeRequest(
    val id: Long,
    val name: String,
    val startTime: String,
    val endTime: String,
    val dateTimeType: DateTimeType,
)
