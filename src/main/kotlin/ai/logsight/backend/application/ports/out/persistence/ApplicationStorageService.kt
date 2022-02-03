package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.users.domain.User
import java.util.*

interface ApplicationStorageService {
    fun createApplication(createApplicationCommand: CreateApplicationCommand): Application
    fun createApplicationWithCallback(createApplicationCommand: CreateApplicationCommand, callback: ((Application) -> Unit)): Application
    fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand)
    fun deleteApplicationWithCallback(deleteApplicationCommand: DeleteApplicationCommand, callback: ((Application) -> Unit))
    fun findApplicationById(applicationId: UUID): Application
    fun findApplicationByUserAndName(user: User, applicationName: String): Optional<Application>
    fun saveApplication(application: Application): Application
}
