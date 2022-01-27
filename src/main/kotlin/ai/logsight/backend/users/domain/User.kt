package ai.logsight.backend.users.domain

import ai.logsight.backend.users.ports.out.persistence.UserType
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val elasticsearchKey: String,
    val email: String,
    var password: String,
    val dateCreated: LocalDateTime,
    val activationDate: LocalDateTime,
    val hasPaid: Boolean,
    val usedData: Long,
    val approachingLimit: Boolean,
    val availableData: Long,
    val activated: Boolean,
    val userType: UserType
)
