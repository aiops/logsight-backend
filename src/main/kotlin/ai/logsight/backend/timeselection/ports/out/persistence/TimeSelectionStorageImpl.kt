package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.extensions.toTimeSelection
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionRepository
import ai.logsight.backend.users.domain.User
import org.springframework.stereotype.Service

@Service
class TimeSelectionStorageImpl(
    private val timeSelectionRepository: TimeSelectionRepository,
) : TimeSelectionStorageService {
    override fun findAllByUser(user: User): List<TimeSelection> {
        return timeSelectionRepository.findAllByUser(user).map { timeSelection -> timeSelection.toTimeSelection() }
    }

    override fun saveTimeSelection(timeSelection: TimeSelectionEntity): TimeSelection {
        return timeSelectionRepository.save(timeSelection).toTimeSelection()
    }

    override fun deleteTimeSelectionById(timeSelectionId: Long) {
        timeSelectionRepository.deleteById(timeSelectionId)
    }

    override fun saveAllTimeSelections(timeSelectionList: List<TimeSelectionEntity>): List<TimeSelection> {
        return timeSelectionRepository.saveAll(timeSelectionList)
            .map { timeSelection -> timeSelection.toTimeSelection() }
    }
}
