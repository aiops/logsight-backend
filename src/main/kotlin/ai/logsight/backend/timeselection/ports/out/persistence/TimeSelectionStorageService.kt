package ai.logsight.backend.timeselection.ports.out.persistence

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.users.domain.User
import java.util.*

interface TimeSelectionStorageService {
    fun findAllByUser(user: User): List<TimeSelection>
    fun saveTimeSelection(timeSelection: TimeSelectionEntity): TimeSelection
    fun deleteTimeSelectionById(timeSelectionId: UUID)
    fun saveAllTimeSelections(timeSelectionList: List<TimeSelectionEntity>): List<TimeSelection>
}
