package ai.logsight.backend.application.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.service.command.CreateApplicationCommand
import ai.logsight.backend.application.service.command.DeleteApplicationCommand

interface ApplicationManagementService {
    fun createApplication(createApplicationCommand: CreateApplicationCommand): Application
    fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand)
}
