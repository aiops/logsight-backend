package ai.logsight.backend.application.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID>
