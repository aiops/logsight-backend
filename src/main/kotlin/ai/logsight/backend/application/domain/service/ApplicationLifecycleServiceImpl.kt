package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    val elasticsearchService: ElasticsearchService,
) : ApplicationLifecycleService {
    private val logger = LoggerImpl(ApplicationLifecycleServiceImpl::class.java)

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(
            createApplicationCommand.applicationName,
            createApplicationCommand.user,
            displayName = createApplicationCommand.displayName
        )
        return applicationStorageService.setApplicationStatus(application, ApplicationStatus.READY)
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val application = applicationStorageService.findApplicationById(deleteApplicationCommand.applicationId)
        if (application.status == ApplicationStatus.CREATING) {
            throw ApplicationStatusException("Application is still creating. Please wait for the application creation process to finish first.")
        }
        logger.info(
            "Setting the application status of ${application.id} to ${ApplicationStatus.DELETING}",
            this::deleteApplication.name
        )
        applicationStorageService.setApplicationStatus(application, ApplicationStatus.DELETING)
        elasticsearchService.deleteESIndices(application.index)
        applicationStorageService.deleteApplication(applicationId = application.id)
    }
}
