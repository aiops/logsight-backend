package ai.logsight.backend.application.ports.out.persistence

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.exceptions.ApplicationNotFoundException
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationStorageServiceImpl(private val appRepository: ApplicationRepository) : ApplicationStorageService {

    val applicationActiveListener = mutableMapOf<UUID, ((Application)->Unit)>()

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        val appEntity = ApplicationEntity(
            name = createApplicationCommand.applicationName,
            status = ApplicationStatus.CREATING,
            user = createApplicationCommand.user.toUserEntity()
        )
        return appRepository.save(appEntity).toApplication()
    }

    override fun createApplicationWithCallback(createApplicationCommand: CreateApplicationCommand, callback: ((Application) -> Unit)): Application {
        val application = createApplication(createApplicationCommand)
        applicationActiveListener[application.id] = callback
        return application
        TODO("needs to notify logsight backend to create application")
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val appEntity = this.findApplicationByIdPrivate(deleteApplicationCommand.applicationId)
        appEntity.status = ApplicationStatus.DELETED
        appRepository.save(appEntity)
        TODO("needs to notify logsight backend to create application")
    }

    override fun deleteApplicationWithCallback(
        deleteApplicationCommand: DeleteApplicationCommand,
        callback: (Application) -> Unit
    ) {
        applicationActiveListener[deleteApplicationCommand.applicationId] = callback
        deleteApplication(deleteApplicationCommand)
        TODO("needs to notify logsight backend to create application")
    }

    private fun findApplicationByIdPrivate(applicationId: UUID): ApplicationEntity {
        return appRepository.findById(applicationId).orElseThrow { ApplicationNotFoundException() }
    }

    override fun findApplicationById(applicationId: UUID): Application {
        return this.findApplicationByIdPrivate(applicationId).toApplication()
    }

    override fun findApplicationByUserAndName(user: User, applicationName: String): Optional<Application> {
        return appRepository.findByUserAndName(user.toUserEntity(), applicationName)
    }

    override fun saveApplication(application: Application): Application {
        return appRepository.save<ApplicationEntity?>(application.toApplicationEntity()).toApplication()
    }
}
