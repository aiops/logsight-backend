package ai.logsight.backend.timeselection.domain

import ai.logsight.backend.timeselection.ports.out.persistence.DateTimeType
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import java.util.*

data class TimeSelection(
    val id: UUID,
    val name: String,
    val startTime: String,
    val endTime: String,
    val dateTimeType: DateTimeType
)
