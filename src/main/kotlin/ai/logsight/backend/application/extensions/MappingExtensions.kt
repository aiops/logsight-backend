package ai.logsight.backend.application.extensions

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity

fun ApplicationEntity.toApplication() = Application(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUser(),
    applicationKey = this.applicationKey
)

fun Application.toApplicationEntity() = ApplicationEntity(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUserEntity(),
    applicationKey = this.applicationKey
)

fun Application.toApplicationDTO() = ApplicationDTO(
    id = this.id,
    userKey = this.user.key,
    name = this.name
)
