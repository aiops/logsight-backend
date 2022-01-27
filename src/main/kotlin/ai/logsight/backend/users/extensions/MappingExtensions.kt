package ai.logsight.backend.users.extensions

import ai.logsight.backend.users.domain.LocalUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.ports.out.persistence.UserEntity
import java.time.LocalDateTime

fun UserEntity.toLocalUser() = LocalUser(
    id = this.id, elasticsearchKey = this.elasticsearchKey
)

fun UserEntity.toUser() = User(
    id = this.id,
    email = this.email,
    password = this.password,
    elasticsearchKey = this.elasticsearchKey,
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
    elasticsearchKey = this.elasticsearchKey,
    activationDate = this.activationDate,
    activated = this.activated,
    usedData = this.usedData,
    approachingLimit = this.approachingLimit,
    availableData = this.availableData,
    hasPaid = this.hasPaid,
    dateCreated = this.dateCreated,
    userType = this.userType
)
