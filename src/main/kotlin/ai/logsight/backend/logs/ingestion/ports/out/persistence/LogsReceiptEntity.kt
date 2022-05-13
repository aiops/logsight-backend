package ai.logsight.backend.logs.ingestion.ports.out.persistence

import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import org.hibernate.annotations.Generated
import org.hibernate.annotations.GenerationTime
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "logs_receipt")
class LogsReceiptEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    // the columnDefinition annotation works only with PostgresDB
    @Generated(GenerationTime.INSERT)
    @Column(name = "order_num", columnDefinition = "serial", nullable = false, unique = true)
    val orderNum: Long = -1,

    @Column(name = "logs_count", nullable = false)
    var logsCount: Int,

    @ManyToOne(cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "application_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    val application: ApplicationEntity,
)
