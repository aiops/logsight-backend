package ai.logsight.backend.application.persistence

import ai.logsight.backend.user.persistence.UserEntity
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: ApplicationStatus,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity
)
