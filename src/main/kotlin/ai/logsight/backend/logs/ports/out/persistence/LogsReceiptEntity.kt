package ai.logsight.backend.logs.ports.out.persistence

import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import org.hibernate.annotations.Generated
import org.hibernate.annotations.GenerationTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "logs_receipt")
class LogsReceiptEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    // the columnDefinition annotation works only with PostgresDB
    @Generated(GenerationTime.INSERT)
    @Column(name = "order_counter", columnDefinition = "serial", nullable = false, unique = true)
    val orderCounter: Long = -1,

    @Column(name = "logs_count", nullable = false)
    var logsCount: Long,

    @Column(name = "source")
    val source: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    val application: ApplicationEntity
)
