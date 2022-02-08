package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.exceptions.ApplicationNotFoundException
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationStorageServiceImpl(private val appRepository: ApplicationRepository) : ApplicationStorageService {

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        val userEntity = createApplicationCommand.user.toUserEntity()

        if (appRepository.findByUserAndName(userEntity, createApplicationCommand.applicationName).isPresent) {
            throw ApplicationAlreadyCreatedException("Application with name ${createApplicationCommand.applicationName} already exists for user.")
        }
        val appEntity = ApplicationEntity(
            name = createApplicationCommand.applicationName, status = ApplicationStatus.CREATING, user = userEntity
        )
        return appRepository.save(appEntity).toApplication()
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val appEntity = this.findApplicationByIdPrivate(deleteApplicationCommand.applicationId)
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

    override fun findApplicationByUserAndName(user: User, applicationName: String): Application? {
        return appRepository.findByUserAndName(user.toUserEntity(), applicationName)
            .orElseThrow { ApplicationNotFoundException("Application $applicationName does not exist for user.") }
            .toApplication()
    }

    override fun findAllApplicationsByUser(user: User): List<Application> {
        return appRepository.findAllByUser(user.toUserEntity()).map { it.toApplication() }
    }

    override fun saveApplication(application: Application): Application {
        return appRepository.save<ApplicationEntity?>(application.toApplicationEntity()).toApplication()
    }
}
