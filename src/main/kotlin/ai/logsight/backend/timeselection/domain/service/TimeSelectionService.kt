package ai.logsight.backend.timeselection.domain.service

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.ports.web.request.PredefinedTimeRequest
import ai.logsight.backend.users.domain.User
import java.util.*

interface TimeSelectionService {
    fun findAllByUser(user: User): List<TimeSelection>
    fun createTimeSelection(user: User, request: PredefinedTimeRequest): TimeSelection
    fun deleteTimeSelection(id: UUID)
    fun createPredefinedTimeSelections(user: User): List<TimeSelection>
}
