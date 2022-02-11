package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.out.rpc.RPCService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    @Qualifier("ZeroMQ") val analyticsManagerAppRPC: RPCService
) : ApplicationLifecycleService {

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(
            createApplicationCommand.applicationName, createApplicationCommand.user
        )
        // create application in backend
        val response = analyticsManagerAppRPC.createApplication(application.toApplicationDTO())
        if (response == null || response.status != HttpStatus.OK) {
            // rollback changes
            applicationStorageService.deleteApplication(applicationId = application.id)

            val msg = response?.message ?: "No response from logsight."
            throw RuntimeException("Logsight failed to create application. Reason: $msg")
        }

        return applicationStorageService.setApplicationStatus(application, ApplicationStatus.READY)
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val application = applicationStorageService.findApplicationById(deleteApplicationCommand.applicationId)
        if (application.status == ApplicationStatus.CREATING) {
            throw ApplicationStatusException("Application is still creating. Please wait for the application creation process to finish first.")
        }
        applicationStorageService.setApplicationStatus(application, ApplicationStatus.DELETING)
        val response = analyticsManagerAppRPC.deleteApplication(application.toApplicationDTO())
        if (response == null || response.status != HttpStatus.OK) {
            val msg = response?.message ?: "No response from logsight."
            throw RuntimeException("Logsight failed to create application. Reason: $msg")
        }

        return applicationStorageService.deleteApplication(applicationId = application.id)
    }
}
