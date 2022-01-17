package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.application.ports.out.persistence.ApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    val analyticsManagerAppRPC: AnalyticsManagerRPC,
) : ApplicationLifecycleService {

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(createApplicationCommand)
        // create application in backend
        analyticsManagerAppRPC.createApplication(application.toApplicationDTO())
        //
        application.status = ApplicationStatus.READY
        return applicationStorageService.saveApplication(application)
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val application = applicationStorageService.findApplicationById(deleteApplicationCommand.applicationId)
        application.status = ApplicationStatus.DELETING
        applicationStorageService.saveApplication(application)
        analyticsManagerAppRPC.deleteApplication(application.toApplicationDTO())
        return applicationStorageService.deleteApplication(deleteApplicationCommand)
    }
}
