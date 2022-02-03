package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity
import ai.logsight.backend.users.domain.User

interface TimeSelectionStorageService {
    fun findAllByUser(user: User): List<TimeSelection>
    fun saveTimeSelection(timeSelection: TimeSelectionEntity): TimeSelection
    fun deleteTimeSelectionById(timeSelectionId: Long)
    fun saveAllTimeSelections(timeSelectionList: List<TimeSelectionEntity>): List<TimeSelection>
}