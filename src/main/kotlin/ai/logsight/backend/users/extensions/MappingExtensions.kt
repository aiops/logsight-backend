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
    userType = this.userType,
    hasPaid = this.hasPaid,
    availableData = this.availableData,
    usedData = this.usedData
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
    userType = this.userType,
    userCategory = this.userCategory

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
    userType = this.userType,
    userCategory = this.userCategory
)

fun OnlineUser.toUser() = User(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activated = this.activated,
    dateCreated = this.dateCreated,
    userType = this.userType,
    hasPaid = this.hasPaid,
    availableData = this.availableData,
    usedData = this.usedData,
    userCategory = this.userCategory
)

fun User.toUserEntity() = UserEntity(
    id = this.id,
    email = this.email,
    password = this.password,
    key = this.key,
    activated = this.activated,
    dateCreated = this.dateCreated,
    userType = this.userType,
    hasPaid = this.hasPaid,
    userCategory = this.userCategory
)
