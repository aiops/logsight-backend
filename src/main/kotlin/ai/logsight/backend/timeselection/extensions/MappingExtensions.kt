package ai.logsight.backend.timeselection.extensions

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.UserEntity

fun TimeSelectionEntity.toTimeSelection() = TimeSelection(
    id = this.id,
    name = this.name,
    startTime = this.startTime,
    endTime = this.endTime,
    dateTimeType = this.dateTimeType,
)

fun TimeSelection.toTimeSelectionEntity(userEntity: UserEntity) = TimeSelectionEntity(
    id = this.id,
    name = this.name,
    startTime = this.startTime,
    endTime = this.endTime,
    dateTimeType = this.dateTimeType,
    user = userEntity
)
