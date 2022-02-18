package ai.logsight.backend.logs.demo

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.logs.domain.enums.LogDataSources
import ai.logsight.backend.logs.domain.enums.LogFormats
import ai.logsight.backend.logs.ingestion.domain.LogsReceipt
import ai.logsight.backend.logs.ingestion.domain.dto.LogBatchDTO
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.utils.LogFileReader
import ai.logsight.backend.users.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.nio.file.Paths

@Service
class LogDemoService(
    private val applicationLifecycleService: ApplicationLifecycleService,
    private val logIngestionService: LogIngestionService
) {
    object SampleLogConstants {
        const val SAMPLE_LOG_DIR = "sample_data"
        const val SAMPLE_TAG = "default"
    }

    fun createHadoopDemoForUser(user: User): List<LogsReceipt> {
        val appNames = listOf("hdfs_node", "node_manager", "resource_manager", "name_node")

        // delete dangling applications if already created

        // create fresh applications
        val applications = appNames.map { name ->
            applicationLifecycleService.createApplication(
                CreateApplicationCommand(
                    name, user
                )
            )
        }

        val logReceipts = applications.map { application ->
            // load sample data
            // todo extract in function
            val resource: URL =
                LogDemoService::class.java.getResource("${SampleLogConstants.SAMPLE_LOG_DIR}/${application.name}")!!
            val file = Paths.get(resource.toURI())
                .toFile()
            val logMessages = LogFileReader().readFile(application.name, file.inputStream())
            logIngestionService.processLogBatch(
                LogBatchDTO(
                    user,
                    application,
                    SampleLogConstants.SAMPLE_TAG,
                    LogFormats.UNKNOWN_FORMAT,
                    logMessages,
                    LogDataSources.SAMPLE
                )
            )
        }
        return logReceipts
    }
}
