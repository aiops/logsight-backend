package ai.logsight.backend.logs.ports.out.persistence

import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "logs_receipt")
class LogsReceiptEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_counter", nullable = false)
    val orderCounter: Long = 0,

    @Column(name = "logs_count", nullable = false)
    var logsCount: Long,

    @Column(name = "source")
    val source: String,

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "app_id")
    val application: ApplicationEntity
)
