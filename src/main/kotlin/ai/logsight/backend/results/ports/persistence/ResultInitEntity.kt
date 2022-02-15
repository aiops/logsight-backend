package ai.logsight.backend.results.ports.persistence

import ai.logsight.backend.logs.ports.out.persistence.LogsReceiptEntity
import ai.logsight.backend.results.domain.service.ResultInitStatus
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "result_init")
class ResultInitEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ResultInitStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logs_receipt_id")
    val logsReceipt: LogsReceiptEntity
)
