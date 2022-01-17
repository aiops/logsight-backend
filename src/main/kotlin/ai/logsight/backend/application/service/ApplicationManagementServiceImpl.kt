package ai.logsight.backend.application.service

import ai.logsight.backend.application.adapters.persistence.ApplicationEntity
import ai.logsight.backend.application.adapters.persistence.service.ApplicationStorageService
import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.service.command.CreateApplicationCommand
import ai.logsight.backend.application.service.command.DeleteApplicationCommand
import ai.logsight.backend.connectors.Connector
import ai.logsight.backend.connectors.ConnectorService
import ai.logsight.backend.connectors.elasticsearch.ESConnector
import org.springframework.stereotype.Service

@Service
class ApplicationManagementServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    val connectorService: ConnectorService
) : ApplicationManagementService {

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(createApplicationCommand)
        // Callback
//        connectorService
        //
        return application
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val app = applicationStorageService.findApplicationById(deleteApplicationCommand.applicationId)
    }
}
