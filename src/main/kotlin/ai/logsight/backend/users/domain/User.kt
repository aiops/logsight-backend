package ai.logsight.backend.users.domain

import ai.logsight.backend.users.ports.out.persistence.UserType
import java.time.LocalDateTime
import java.util.*

data class User(
    val id: UUID,
    val key: String,
    val email: String,
    var password: String,
    val dateCreated: LocalDateTime,
    val activated: Boolean,
    val hasPaid: Boolean,
    val availableData: Long,
    val usedData: Long,
    val stripeId: String?,
    val userType: UserType = UserType.LOCAL_USER,
    val userCategory: UserCategory
)
