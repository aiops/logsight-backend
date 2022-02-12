package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.ApplicationStatus
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationRemoteException
import ai.logsight.backend.application.exceptions.ApplicationStatusException
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.out.rpc.RPCService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    @Qualifier("ZeroMQ") val analyticsManagerAppRPC: RPCService
) : ApplicationLifecycleService {
    val logger: Logger = LoggerFactory.getLogger(ApplicationLifecycleServiceImpl::class.java)

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(
            createApplicationCommand.applicationName, createApplicationCommand.user
        )

        fun handleException(message: String) {
            logger.debug("Rolling back changes. Deleting application")
            applicationStorageService.deleteApplication(applicationId = application.id)
            throw ApplicationRemoteException(message)
        }
        // create application in backend
        try {

            val response = analyticsManagerAppRPC.createApplication(application.toApplicationDTO())
            if (response.status != HttpStatus.OK) {
                // rollback changes
                handleException(response.message)
            }
        } catch (ex: ApplicationRemoteException) {
            logger.error(ex.message)
            // rollback changes
            handleException(ex.message.toString())
        }
        logger.info("Application ${application.name} created.")
        return applicationStorageService.setApplicationStatus(application, ApplicationStatus.READY)
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val application = applicationStorageService.findApplicationById(deleteApplicationCommand.applicationId)
        if (application.status == ApplicationStatus.CREATING) {
            throw ApplicationStatusException("Application is still creating. Please wait for the application creation process to finish first.")
        }
        applicationStorageService.setApplicationStatus(application, ApplicationStatus.DELETING)

        try {

            val response = analyticsManagerAppRPC.deleteApplication(application.toApplicationDTO())
            if (response.status != HttpStatus.OK) {
                throw ApplicationRemoteException(response.message)
            }
        } catch (ex: ApplicationRemoteException) {
            logger.error(ex.message)
            // rollback changes
            throw ApplicationRemoteException(ex.message)
        } finally {
            applicationStorageService.deleteApplication(applicationId = application.id)
        }
    }
}
