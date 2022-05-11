package ai.logsight.backend.application.extensions

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.exceptions.ApplicationStatusException

fun Application.isReady(): Boolean {
    return this.status == ApplicationStatus.READY
}

fun Application.isReadyOrException(): Boolean {
    if (this.status != ApplicationStatus.READY) {
        throw ApplicationStatusException(
            "To receive logs, tha application ${this.name} must be in state " +
                "${ApplicationStatus.READY.name} but is currently in state ${this.status.name}."
        )
    }
    return true
}

fun Application.isCreating(): Boolean {
    return this.status == ApplicationStatus.CREATING
}
