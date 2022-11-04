package ai.logsight.backend.logwriter.ports.web.out.persistance

import ai.logsight.backend.users.ports.out.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LogWriterRepository : JpaRepository<LogWriterEntity, UUID> {

    fun findAllByUser(user: UserEntity): MutableList<LogWriterEntity>
}
