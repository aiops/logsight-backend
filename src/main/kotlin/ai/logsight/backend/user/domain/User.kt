package ai.logsight.backend.user.domain

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val privateKey: String,
    val email: String,
    var password: String,
    val dateCreated: LocalDateTime,
    val activationDate: LocalDateTime,
    val hasPaid: Boolean,
    val usedData: Long,
    val approachingLimit: Boolean,
    val availableData: Long,
    val activated: Boolean
)
