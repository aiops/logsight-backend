package ai.logsight.backend.users.extensions

import ai.logsight.backend.users.domain.OnlineUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import java.time.LocalDateTime

fun UserEntity.toUser() = User(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activated = this.activated,
    dateCreated = this.dateCreated ?: LocalDateTime.now(),
    userType = this.userType
)

fun UserEntity.toOnlineUser() = OnlineUser(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activationDate = this.activationDate ?: LocalDateTime.now(),
    activated = this.activated,
    usedData = this.usedData,
    approachingLimit = this.approachingLimit,
    availableData = this.availableData,
    hasPaid = this.hasPaid,
    dateCreated = this.dateCreated ?: LocalDateTime.now(),
    userType = this.userType

)

fun OnlineUser.toUserEntity() = UserEntity(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activationDate = this.activationDate,
    activated = this.activated,
    usedData = this.usedData,
    approachingLimit = this.approachingLimit,
    availableData = this.availableData,
    hasPaid = this.hasPaid,
    dateCreated = this.dateCreated,
    userType = this.userType
)

fun OnlineUser.toUser() = User(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activated = this.activated,
    dateCreated = this.dateCreated,
    userType = this.userType
)

fun User.toUserEntity() = UserEntity(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activated = this.activated,
    dateCreated = this.dateCreated,
    userType = this.userType
)
