package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.security.KeyGenerator
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "application_key", nullable = false)
    var applicationKey: String = KeyGenerator.generate(),

    @Column(name = "name")
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ApplicationStatus,

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "user_id")
    val user: UserEntity
)
