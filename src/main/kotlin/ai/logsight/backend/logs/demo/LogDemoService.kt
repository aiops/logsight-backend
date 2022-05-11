package ai.logsight.backend.logs.demo

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.utils.LogFileReader
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUserEntity
import org.springframework.stereotype.Service

@Service
class LogDemoService(
    private val applicationLifecycleService: ApplicationLifecycleService,
    private val applicationRepository: ApplicationRepository,
    private val logIngestionService: LogIngestionService
) {
    val logger: LoggerImpl = LoggerImpl(LogDemoService::class.java)

    object SampleLogConstants {
        const val SAMPLE_LOG_DIR = "sample_data"
    }

    fun createHadoopDemoForUser(user: User): List<LogsReceipt> {
        val appNames = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")

        // delete dangling applications if already created
        appNames.forEach { name ->
            val app = applicationRepository.findByUserAndName(user.toUserEntity(), name)
            if (app != null) {
                applicationLifecycleService.deleteApplication(DeleteApplicationCommand(app.id, user))
            }
        }

        // create fresh applications
        val applications = appNames.map { name ->
            applicationLifecycleService.createApplication(
                CreateApplicationCommand(name, user)
            )
        }

        val logReceipts = applications.map { application ->
            // load sample data
            // todo extract in function
            val fileAsInputStream = LogDemoService::class.java.classLoader.getResourceAsStream(
                "${SampleLogConstants.SAMPLE_LOG_DIR}/${application.name}"
            )!!
            val logMessages = LogFileReader().readDemoFile(application.name, fileAsInputStream)

            logIngestionService.processLogBatch(
                LogBatch(
                    application = application,
                    logs = logMessages,
                )
            )
        }
        return logReceipts
    }
}
