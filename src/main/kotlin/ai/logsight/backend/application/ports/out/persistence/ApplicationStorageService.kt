package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.users.domain.User
import java.util.*

interface ApplicationStorageService {
    fun createApplication(createApplicationCommand: CreateApplicationCommand): Application
    fun deleteApplication(applicationId: UUID)
    fun findApplicationById(applicationId: UUID): Application
    fun applicationByIdExists(applicationId: UUID): Boolean
    fun findApplicationByUserAndName(user: User, applicationName: String): Application?
    fun applicationByUserAndNameExists(user: User, applicationName: String): Boolean
    fun findAllApplicationsByUser(user: User): List<Application>
    fun setApplicationStatus(application: Application, applicationStatus: ApplicationStatus): Application
}
