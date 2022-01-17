package ai.logsight.backend.user.extensions

import ai.logsight.backend.user.adapters.persistence.UserEntity
import ai.logsight.backend.user.domain.LocalUser
import ai.logsight.backend.user.domain.User
import java.time.LocalDateTime

fun UserEntity.toLocalUser() = LocalUser(
    id = this.id, privateKey = this.privateKey
)

fun UserEntity.toUser() = User(
    id = this.id,
    email = this.email,
    password = this.password,
    privateKey = this.privateKey,
    activationDate = this.activationDate ?: LocalDateTime.now(),
    activated = this.activated,
    usedData = this.usedData,
    approachingLimit = this.approachingLimit,
    availableData = this.availableData,
    hasPaid = this.hasPaid,
    dateCreated = this.dateCreated ?: LocalDateTime.now(),
    userType = this.userType

)

fun User.toUserEntity() = UserEntity(
    id = this.id,
    email = this.email,
    password = this.password,
    privateKey = this.privateKey,
    activationDate = this.activationDate,
    activated = this.activated,
    usedData = this.usedData,
    approachingLimit = this.approachingLimit,
    availableData = this.availableData,
    hasPaid = this.hasPaid,
    dateCreated = this.dateCreated,
    userType = this.userType
)
