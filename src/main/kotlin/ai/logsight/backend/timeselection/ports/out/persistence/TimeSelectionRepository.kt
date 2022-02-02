package ai.logsight.backend.timeselection.ports.out.persistence

import ai.logsight.backend.users.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TimeSelectionRepository : JpaRepository<TimeSelectionEntity, Long> {
    fun findAllByUser(user: User): List<TimeSelectionEntity>
}
