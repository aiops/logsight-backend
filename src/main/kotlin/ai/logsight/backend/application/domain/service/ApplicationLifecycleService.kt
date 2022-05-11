package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand

interface ApplicationLifecycleService {
    fun createApplication(createApplicationCommand: CreateApplicationCommand): Application
    fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand)
}
