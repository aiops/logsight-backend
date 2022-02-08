package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.users.ports.out.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID> {
    fun findByUserAndName(user: UserEntity, applicationName: String): Optional<ApplicationEntity>
    fun findAllByUser(user: UserEntity): List<ApplicationEntity>
}
