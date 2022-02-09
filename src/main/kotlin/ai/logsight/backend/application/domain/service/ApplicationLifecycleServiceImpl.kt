package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.exceptions.ElasticsearchException
import ai.logsight.backend.exceptions.LogsightApplicationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    @Qualifier("ZeroMQ") val analyticsManagerAppRPC: AnalyticsManagerRPC,
    val elasticsearchService: ElasticsearchService

) : ApplicationLifecycleService {

    val indexPatters = listOf("log_ad", "count_ad", "incidents", "log_agg", "log_quality") // TODO: EXTRACT in config.

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(createApplicationCommand)
        // create application in backend
        val response = analyticsManagerAppRPC.createApplication(application.toApplicationDTO())
        if (response == null || response.status != HttpStatus.OK) {
            // rollback changes
            applicationStorageService.deleteApplication(
                DeleteApplicationCommand(
                    applicationId = application.id, user = createApplicationCommand.user
                )
            )
            val msg = response?.message ?: "No response from logsight."
            throw LogsightApplicationException("Logsight failed to create application. Reason: $msg")
        }

        // create index patterns
        try {
            elasticsearchService.createKibanaIndexPatterns(
                createApplicationCommand.user.key, application.name, indexPatters
            )
        } catch (e: ElasticsearchException) {
            // rollback changes
            applicationStorageService.deleteApplication(
                DeleteApplicationCommand(
                    applicationId = application.id, user = createApplicationCommand.user
                )
            )
            throw ElasticsearchException(e.message)
        }
        //
        application.status = ApplicationStatus.READY
        return applicationStorageService.saveApplication(application)
    }

    override fun deleteApplication(deleteApplicationCommand: DeleteApplicationCommand) {
        val application = applicationStorageService.findApplicationById(deleteApplicationCommand.applicationId)
        application.status = ApplicationStatus.DELETING
        applicationStorageService.saveApplication(application)
        analyticsManagerAppRPC.deleteApplication(application.toApplicationDTO())
        elasticsearchService.deleteKibanaIndexPatterns(
            deleteApplicationCommand.user.key, application.name, indexPatters
        )
        return applicationStorageService.deleteApplication(deleteApplicationCommand)
    }
}
