package ai.logsight.backend.application.persistence.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.service.command.CreateApplicationCommand
import ai.logsight.backend.application.service.command.DeleteApplicationCommand
import java.util.*

class ApplicationStorageServiceImpl : ApplicationStorageService {
    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        TODO("Not yet implemented")
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand): Application {
        TODO("Not yet implemented")
    }

    override fun findApplicationById(applicationId: UUID): Application {
        TODO("Not yet implemented")
    }

    override fun saveApplication(application: Application): Application {
        TODO("Not yet implemented")
    }
}
