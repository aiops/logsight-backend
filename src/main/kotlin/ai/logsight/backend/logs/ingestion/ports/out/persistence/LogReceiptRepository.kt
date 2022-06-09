package ai.logsight.backend.logs.ingestion.ports.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LogReceiptRepository : JpaRepository<LogReceiptEntity, UUID>
