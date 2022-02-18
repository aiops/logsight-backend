package ai.logsight.backend.timeselection.ports.out.persistence

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.extensions.toTimeSelection
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class TimeSelectionStorageImpl(
    private val timeSelectionRepository: TimeSelectionRepository,
) : TimeSelectionStorageService {
    override fun findAllByUser(user: User): List<TimeSelection> {
        return timeSelectionRepository.findAllByUser(user.toUserEntity())
            .map { timeSelection -> timeSelection.toTimeSelection() }
    }

    override fun saveTimeSelection(timeSelection: TimeSelectionEntity): TimeSelection {
        return timeSelectionRepository.save(timeSelection)
            .toTimeSelection()
    }

    override fun deleteTimeSelectionById(timeSelectionId: UUID) {
        timeSelectionRepository.deleteById(timeSelectionId)
    }

    override fun saveAllTimeSelections(timeSelectionList: List<TimeSelectionEntity>): List<TimeSelection> {
        return timeSelectionRepository.saveAll(timeSelectionList)
            .map { timeSelection -> timeSelection.toTimeSelection() }
    }
}
