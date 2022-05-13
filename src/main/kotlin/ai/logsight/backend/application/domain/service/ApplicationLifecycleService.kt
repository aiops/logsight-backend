package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand

interface ApplicationLifecycleService {
    fun createApplication(createAppCmd: CreateApplicationCommand): Application
    fun autoCreateApplication(createAppCmd: CreateApplicationCommand): Application
    fun deleteApplication(deleteAppCmd: DeleteApplicationCommand)
}
