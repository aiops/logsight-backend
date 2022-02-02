package ai.logsight.backend.timeselection.extensions

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity

fun TimeSelectionEntity.toTimeSelection() = TimeSelection(
    id = this.id,
    name = this.name,
    startTime = this.startTime,
    endTime = this.endTime,
    dateTimeType = this.dateTimeType,
    user = this.user
)

fun TimeSelection.toTimeSelectionEntity() = TimeSelectionEntity(
    id = this.id,
    name = this.name,
    startTime = this.startTime,
    endTime = this.endTime,
    dateTimeType = this.dateTimeType,
    user = this.user
)
