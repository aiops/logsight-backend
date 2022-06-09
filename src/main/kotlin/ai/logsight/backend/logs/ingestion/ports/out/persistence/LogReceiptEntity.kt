package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.logs.ingestion.domain.enums.LogBatchStatus
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "logs_receipt")
class LogReceiptEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "logs_count", nullable = false)
    val logsCount: Int,

    @Column(name = "processed_log_count", nullable = false)
    val processedLogCount: Int = 0,

    @Column(name = "batch_id", nullable = false)
    val batchId: UUID,

    @Column(name = "status", nullable = false)
    var status: LogBatchStatus
)
