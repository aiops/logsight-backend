package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.application.utils.NameParser
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationStorageServiceImpl(private val appRepository: ApplicationRepository) : ApplicationStorageService {
    private val logger = LoggerImpl(ApplicationStorageServiceImpl::class.java)

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        val appEntity = ApplicationEntity(
            displayName = createApplicationCommand.displayName,
            name = createApplicationCommand.applicationName,
            index = createApplicationCommand.elasticsearchIndex,
            status = ApplicationStatus.READY,
            user = createApplicationCommand.user.toUserEntity(),
        )
        return appRepository.save(appEntity).toApplication()
    }

    override fun deleteApplication(applicationId: UUID) =
        this.findApplicationByIdPrivate(applicationId)?.let { appRepository.delete(it) }
            ?: throw ApplicationNotFoundException("Application $applicationId does not exist for user.")

    private fun findApplicationByIdPrivate(applicationId: UUID): ApplicationEntity? {
        return appRepository.findById(applicationId).orElse(null)
    }

    override fun findApplicationById(applicationId: UUID): Application =
        this.findApplicationByIdPrivate(applicationId)?.toApplication()
            ?: throw ApplicationNotFoundException("Application $applicationId does not exist for user.")

    override fun applicationByIdExists(applicationId: UUID): Boolean =
        this.findApplicationByIdPrivate(applicationId) != null

    private fun findApplicationByUserAndNamePrivate(user: User, applicationName: String): ApplicationEntity? =
        appRepository.findByUserAndName(user.toUserEntity(), applicationName)
    
    override fun findApplicationByUserAndName(user: User, applicationName: String): Application =
        findApplicationByUserAndNamePrivate(user, applicationName)?.toApplication()
            ?: throw ApplicationNotFoundException("Application $applicationName does not exist for user ${user.id}.")

    override fun applicationByUserAndNameExists(user: User, applicationName: String): Boolean =
        findApplicationByUserAndNamePrivate(user, applicationName) != null

    override fun findAllApplicationsByUser(user: User): List<Application> {
        return appRepository.findAllByUser(user.toUserEntity()).map { it.toApplication() }
    }

    override fun setApplicationStatus(application: Application, applicationStatus: ApplicationStatus): Application {
        val appEntityChanged = findApplicationByIdPrivate(applicationId = application.id)
            ?.toApplication()
            ?.copy(status = applicationStatus)
            ?.toApplicationEntity()
            ?: throw ApplicationNotFoundException("Application $application does not exist.")
        return appRepository.save(appEntityChanged).toApplication()
    }
}
