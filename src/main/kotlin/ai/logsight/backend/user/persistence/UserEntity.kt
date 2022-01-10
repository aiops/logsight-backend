package ai.logsight.backend.user.persistence

import ai.logsight.backend.security.KeyGenerator
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "private_key", nullable = false)
    var privateKey: String = KeyGenerator.generate(),

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
    var activated: Boolean = false
)
