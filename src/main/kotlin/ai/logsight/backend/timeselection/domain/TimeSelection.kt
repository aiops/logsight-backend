package ai.logsight.backend.timeselection.domain

import ai.logsight.backend.timeselection.ports.out.persistence.DateTimeType
import ai.logsight.backend.users.ports.out.persistence.UserEntity

data class TimeSelection(
    val id: Long,
    val name: String,
    val startTime: String,
    val endTime: String,
    val dateTimeType: DateTimeType,
    val user: UserEntity
)
