package ai.logsight.backend.results.ports.persistence

import ai.logsight.backend.results.domain.service.ResultInitStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ResultInitRepository : JpaRepository<ResultInitEntity, UUID> {
    fun findAllByStatusAndLogsReceipt_Application_Id(status: ResultInitStatus, logReceiptApplicationId: UUID): List<ResultInitEntity>
}
