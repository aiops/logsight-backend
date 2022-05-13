package ai.logsight.backend.application.extensions

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.ports.dto.ApplicationDTO
import ai.logsight.backend.application.ports.dto.ApplicationDTOActions
import ai.logsight.backend.application.ports.out.persistence.ApplicationEntity
import ai.logsight.backend.application.ports.web.responses.ApplicationResponse
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity

fun ApplicationEntity.toApplication() = Application(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUser(),
    applicationKey = this.applicationKey,
    displayName = this.displayName ?: "",
    index = this.index
)

fun Application.toApplicationEntity() = ApplicationEntity(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUserEntity(),
    applicationKey = this.applicationKey,
    displayName = this.displayName,
    index = this.index
)

fun Application.toApplicationDTO(action: ApplicationDTOActions) = ApplicationDTO(
    id = this.id,
    userKey = this.user.key,
    name = this.name,
    index = this.index,
    action = action
)

fun Application.toApplicationResponse() = ApplicationResponse(
    applicationId = this.id,
    name = this.name,
    displayName = this.displayName ?: ""
)

fun Application.toApplicationStatus(status: ApplicationStatus) = Application(
    id = this.id,
    name = this.name,
    status = status,
    user = this.user,
    applicationKey = this.applicationKey,
    displayName = this.displayName,
    index = this.index
)
