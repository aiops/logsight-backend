package ai.logsight.backend.application.ports.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID>
