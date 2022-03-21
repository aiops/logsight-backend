package ai.logsight.backend.flush.ports.persistence

import ai.logsight.backend.flush.domain.service.FlushStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FlushRepository : JpaRepository<FlushEntity, UUID> {
    fun findAllByStatusAndLogsReceipt_Application_Id(status: FlushStatus, logReceiptApplicationId: UUID): List<FlushEntity>
}
