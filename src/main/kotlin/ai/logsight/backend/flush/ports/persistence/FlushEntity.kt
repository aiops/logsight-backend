package ai.logsight.backend.flush.ports.persistence

import ai.logsight.backend.flush.domain.service.FlushStatus
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptEntity
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "flush")
class FlushEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: FlushStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logs_receipt_id")
    val logsReceipt: LogsReceiptEntity
)
