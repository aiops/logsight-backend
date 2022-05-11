package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
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
    private val nameParser = NameParser()

    override fun createApplication(applicationName: String, user: User, displayName: String?): Application {
        val userEntity = user.toUserEntity()

        if (appRepository.findByUserAndName(userEntity, applicationName) != null) {
            logger.error(
                "Application with name $applicationName already exists for user ${user.id}.",
                this::createApplication.name
            )
            throw ApplicationAlreadyCreatedException("Application with name $applicationName already exists for user.")
        }
        val appEntity = ApplicationEntity(
            displayName = displayName,
            name = applicationName,
            index = nameParser.toElasticsearchStandard(applicationName),
            status = ApplicationStatus.CREATING,
            user = userEntity,
        )
        return appRepository.save(appEntity).toApplication()
    }

    override fun deleteApplication(applicationId: UUID) {
        val appEntity = this.findApplicationByIdPrivate(applicationId)
        appEntity.status = ApplicationStatus.DELETED
        appRepository.delete(appEntity)
    }

    private fun findApplicationByIdPrivate(applicationId: UUID): ApplicationEntity {
        return appRepository.findById(applicationId)
            .orElseThrow { ApplicationNotFoundException("Application $applicationId does not exist for user.") }
    }

    override fun findApplicationById(applicationId: UUID): Application {
        return this.findApplicationByIdPrivate(applicationId).toApplication()
    }

    override fun findApplicationByUserAndName(user: User, applicationName: String): Application =
        appRepository.findByUserAndName(user.toUserEntity(), applicationName)?.toApplication()
            ?: throw ApplicationNotFoundException("Application $applicationName does not exist for user ${user.id}.")

    override fun findAllApplicationsByUser(user: User): List<Application> {
        return appRepository.findAllByUser(user.toUserEntity()).map { it.toApplication() }
    }

    override fun saveApplication(application: Application): Application {
        return appRepository.save(application.toApplicationEntity()).toApplication()
    }

    override fun setApplicationStatus(application: Application, applicationStatus: ApplicationStatus): Application {
        val appEntity = application.toApplicationEntity()
        appEntity.status = applicationStatus
        appRepository.save(appEntity)
        return appEntity.toApplication()
    }

    override fun autoCreateApplication(applicationName: String, user: User, displayName: String?): Application {
        val userEntity = user.toUserEntity()
        val app = appRepository.findByUserAndName(userEntity, applicationName)
        if (app != null) {
            return app.toApplication()
        }
        val appEntity = ApplicationEntity(
            displayName = applicationName,
            name = applicationName,
            index = nameParser.toElasticsearchStandard(applicationName),
            status = ApplicationStatus.CREATING,
            user = userEntity,
        )
        return appRepository.save(appEntity).toApplication()
    }
}
