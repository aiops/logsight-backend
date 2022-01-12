package ai.logsight.backend.application.extensions

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.persistence.ApplicationEntity
import ai.logsight.backend.user.extensions.toUser

fun ApplicationEntity.toApplication() = Application(
    id = this.id,
    name = this.name,
    status = this.status,
    user = this.user.toUser()

)
