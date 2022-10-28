package ai.logsight.backend.autolog.ports.web.out.persistance

import ai.logsight.backend.security.KeyGenerator
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "autolog")
class AutoLogEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "is_helpful", nullable = true)
    val isHelpful: Boolean? = null,

    @Column(name = "language", nullable = false)
    val language: String = "python",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity
)
