package ai.logsight.backend.timeselection.ports.out.persistence

import ai.logsight.backend.users.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TimeSelectionRepository : JpaRepository<TimeSelectionEntity, UUID> {
    fun findAllByUser(user: User): List<TimeSelectionEntity>
}
