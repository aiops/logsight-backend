package ai.logsight.backend.timeselection.domain.service

import ai.logsight.backend.timeselection.domain.TimeSelection
import ai.logsight.backend.timeselection.ports.web.request.PredefinedTimeRequest
import ai.logsight.backend.users.domain.User

interface TimeSelectionService {
    fun findAllByUser(user: User): List<TimeSelection>
    fun createTimeSelection(user: User, request: PredefinedTimeRequest): TimeSelection
    fun deleteTimeSelection(id: Long)
    fun createPredefinedTimeSelections(user: User): List<TimeSelection>
}
