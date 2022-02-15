package ai.logsight.backend.logs.ports.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LogsReceiptRepository : JpaRepository<LogsReceiptEntity, UUID> {
    fun findAllByOrderNum(orderNum: Long): List<LogsReceiptEntity>
}
