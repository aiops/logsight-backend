package ai.logsight.backend.application.domain.service

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.exceptions.ApplicationAlreadyCreatedException
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.utils.NameParser
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchException
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.users.domain.User
import org.springframework.stereotype.Service

@Service
class ApplicationLifecycleServiceImpl(
    val applicationStorageService: ApplicationStorageService,
    val elasticsearchService: ElasticsearchService,
) : ApplicationLifecycleService {
    private val logger = LoggerImpl(ApplicationLifecycleServiceImpl::class.java)
    private val nameParser = NameParser()

    private fun toElasticsearchIndex(user: User, applicationName: String) =
        "${user.key}_${nameParser.toElasticsearchStandard(applicationName)}"

    private fun createApplicationPrivate(createAppCmd: CreateApplicationCommand): Application {
        val createApplicationCommandEsComplete = createAppCmd.copy(
            elasticsearchIndex = toElasticsearchIndex(createAppCmd.user, createAppCmd.applicationName)
        )
        return applicationStorageService.createApplication(createApplicationCommandEsComplete)
    }

    override fun createApplication(createAppCmd: CreateApplicationCommand): Application {
        if (applicationStorageService.applicationByUserAndNameExists(createAppCmd.user, createAppCmd.applicationName)) {
            val msg =
                "Application with name ${createAppCmd.applicationName} already exists for user ${createAppCmd.user.id}."
            logger.error(msg)
            throw ApplicationAlreadyCreatedException(msg)
        }
        return createApplicationPrivate(createAppCmd)
    }

    override fun autoCreateApplication(createAppCmd: CreateApplicationCommand): Application =
        if (applicationStorageService.applicationByUserAndNameExists(createAppCmd.user, createAppCmd.applicationName)) {
            applicationStorageService.findApplicationByUserAndName(createAppCmd.user, createAppCmd.applicationName)
        } else {
            this.createApplicationPrivate(createAppCmd)
        }

    override fun deleteApplication(deleteAppCmd: DeleteApplicationCommand) {
        val application = applicationStorageService.findApplicationById(deleteAppCmd.applicationId)
        logger.info("Deleting application ${application.id}", this::deleteApplication.name)
        try {
            elasticsearchService.deleteESIndices(application.index)
        } catch (ex: ElasticsearchException) {
            logger.error("Application ${application.name}: $ex")
        }
        applicationStorageService.deleteApplication(applicationId = application.id)
    }
}
