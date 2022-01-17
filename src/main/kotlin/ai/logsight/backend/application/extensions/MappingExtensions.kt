package ai.logsight.backend.application.extensions

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.ports.out.rpc.dto.ApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.user.extensions.toUser
import ai.logsight.backend.user.extensions.toUserEntity

fun ApplicationEntity.toApplication() = Application(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUser(),
)

fun Application.toApplicationEntity() = ApplicationEntity(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUserEntity(),
)

fun Application.toApplicationDTO() = ApplicationDTO(
    id = this.id,
    name = this.name
)
