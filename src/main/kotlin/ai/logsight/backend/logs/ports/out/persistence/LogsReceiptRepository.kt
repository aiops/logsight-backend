package ai.logsight.backend.logs.ports.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface LogsReceiptRepository : JpaRepository<LogsReceiptEntity, Long> {
    fun findAllByOrderByOrderCounterAsc(): List<LogsReceiptEntity>
}
