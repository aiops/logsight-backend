package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.users.domain.User
import java.util.*

interface ApplicationStorageService {
    fun createApplication(applicationName: String, user: User, displayName: String? = null): Application
    fun deleteApplication(applicationId: UUID)
    fun findApplicationById(applicationId: UUID): Application
    fun findApplicationByUserAndName(user: User, applicationName: String): Application
    fun findAllApplicationsByUser(user: User): List<Application>
    fun setApplicationStatus(application: Application, applicationStatus: ApplicationStatus): Application
    fun autoCreateApplication(applicationName: String, user: User, displayName: String? = null): Application
}
