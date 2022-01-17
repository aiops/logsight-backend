package ai.logsight.backend.application.adapters.persistence.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.service.command.CreateApplicationCommand
import ai.logsight.backend.application.service.command.DeleteApplicationCommand
import java.util.*

interface ApplicationStorageService {
    fun createApplication(createApplicationCommand: CreateApplicationCommand): Application
    fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand)
    fun findApplicationById(applicationId: UUID): Application
    fun saveApplication(application: Application): Application
}
