package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptEntity
import ai.logsight.backend.security.KeyGenerator
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "application_key", nullable = false)
    var applicationKey: String = KeyGenerator.generate(),

    @Column(name = "display_name", nullable = true)
    val displayName: String? = null,

    @Column(name = "index", unique = true)
    val index: String,

    @Column(name = "name")
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ApplicationStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL], orphanRemoval = true)
    @Column(name = "logs_receipts")
    val logsReceipts: List<LogsReceiptEntity> = listOf()
)
