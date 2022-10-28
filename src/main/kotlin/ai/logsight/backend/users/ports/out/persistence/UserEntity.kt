package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.autolog.ports.web.out.persistance.AutoLogEntity
import ai.logsight.backend.security.KeyGenerator
import ai.logsight.backend.timeselection.ports.out.persistence.TimeSelectionEntity
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "key", nullable = false, unique = true)
    var key: String = KeyGenerator.generate(),

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "date_created", nullable = false)
    val dateCreated: LocalDateTime? = LocalDateTime.now(),

    @Column(name = "activation_date", nullable = true)
    val activationDate: LocalDateTime? = null,

    @Column(name = "has_paid", nullable = false)
    val hasPaid: Boolean = false,

    @Column(name = "used_data")
    val usedData: Long = 0L,

    @Column(name = "approaching_limit")
    val approachingLimit: Boolean = false,

    @Column(name = "available_data")
    val availableData: Long = 1000000000L,

    @Column(name = "activated", nullable = false)
    var activated: Boolean = false,

    @Column(name = "user_type")
    val userType: UserType = UserType.ONLINE_USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @Column(name = "time_selection_entities")
    val timeSelectionEntities: List<TimeSelectionEntity> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @Column(name = "autolog_entities")
    val autoLogEntities: List<AutoLogEntity> = listOf()
)
