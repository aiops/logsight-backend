package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.extensions.toApplicationDTO
import ai.logsight.backend.application.ports.out.persistence.ApplicationStatus
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.out.rpc.AnalyticsManagerRPC
import ai.logsight.backend.elasticsearch.ElasticsearchService
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    val analyticsManagerAppRPC: AnalyticsManagerRPC,
    val elasticsearchService: ElasticsearchService

) : ApplicationLifecycleService {

    val indexPatters = listOf("log_ad", "count_ad", "incidents", "log_agg", "log_quality") // TODO: EXTRACT in config.

    override fun createApplication(createApplicationCommand: CreateApplicationCommand): Application {
        // Create application
        val application = applicationStorageService.createApplication(createApplicationCommand)
        // create application in backend
        analyticsManagerAppRPC.createApplication(application.toApplicationDTO())

        // create index patterns
        elasticsearchService.createKibanaIndexPatterns(
            createApplicationCommand.user.key, application.applicationKey, indexPatters
        )
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
