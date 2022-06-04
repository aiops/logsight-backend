package ai.logsight.backend.logs.demo

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationRepository
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
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

    fun deleteDanglingApplications(user: User) {
        val applicationNames = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")
        // delete dangling applications if already created
        applicationNames.forEach { name ->
            val app = applicationRepository.findByUserAndName(user.toUserEntity(), name)
            if (app != null) {
                applicationLifecycleService.deleteApplication(DeleteApplicationCommand(app.id, user))
            }
        }
    }

    fun createHadoopDemoForUser(user: User): List<LogsReceipt> {
        deleteDanglingApplications(user)

        val fileNames = listOf("hdfs_node-v1.0.0", "hdfs_node-v1.1.0", "node_manager-v1.0.0", "node_manager-v1.1.0", "resource_manager-v1.0.0", "resource_manager-v1.1.0", "name_node-v1.0.0", "name_node-v1.1.0")
        val logReceipts = fileNames.map { fileName ->
            // read file
            val logMessages = this.readSampleFile(fileName)
            // create application
            val appName = fileName.split("-")[0]
            val application = applicationLifecycleService.createApplication(
                CreateApplicationCommand(
                    applicationName = appName,
                    user = user,
                    displayName = appName
                )
            )
            // process logs
            logIngestionService.processLogBatch(
                LogBatch(
                    application = application,
                    logs = logMessages,
                )
            )
        }.toList()

        return logReceipts
    }

    private fun readSampleFile(fileName: String): List<LogsightLog> {
        val fileAsInputStream = LogDemoService::class.java.classLoader.getResourceAsStream(
            "${SampleLogConstants.SAMPLE_LOG_DIR}/$fileName"
        )!!
        return LogFileReader().readDemoFile(fileName, fileAsInputStream)
    }
}
