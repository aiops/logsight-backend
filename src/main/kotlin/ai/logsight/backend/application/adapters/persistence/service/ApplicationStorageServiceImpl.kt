package ai.logsight.backend.application.adapters.persistence.service

import ai.logsight.backend.application.adapters.persistence.ApplicationEntity
import ai.logsight.backend.application.adapters.persistence.ApplicationRepository
import ai.logsight.backend.application.adapters.persistence.ApplicationStatus
import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.extensions.toApplication
import ai.logsight.backend.application.extensions.toApplicationEntity
import ai.logsight.backend.application.service.command.CreateApplicationCommand
import ai.logsight.backend.application.service.command.DeleteApplicationCommand
import ai.logsight.backend.exceptions.ApplicationNotFoundException
import ai.logsight.backend.user.extensions.toUserEntity
import java.util.*

class ApplicationStorageServiceImpl(private val appRepository: ApplicationRepository) : ApplicationStorageService {
    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        val appEntity = ApplicationEntity(
            name = createApplicationCommand.applicationName,
            status = ApplicationStatus.CREATING,
            user = createApplicationCommand.user.toUserEntity()
        )
        return appRepository.save(appEntity).toApplication()
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val appEntity = this.findApplicationByIdPrivate(deleteApplicationCommand.applicationId)
        appRepository.delete(appEntity)
    }

    private fun findApplicationByIdPrivate(applicationId: UUID): ApplicationEntity {
        return appRepository.findById(applicationId).orElseThrow { ApplicationNotFoundException() }
    }

    override fun findApplicationById(applicationId: UUID): Application {
        return this.findApplicationByIdPrivate(applicationId).toApplication()
    }

    override fun saveApplication(application: Application): Application {
        return appRepository.save<ApplicationEntity?>(application.toApplicationEntity()).toApplication()
    }
}
