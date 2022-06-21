package ai.logsight.backend.logs.demo

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.elasticsearch.ElasticsearchService
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.utils.LogFileReader
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.logs.ingestion.domain.LogReceipt
import org.springframework.stereotype.Service

@Service
class LogDemoService(
    private val logIngestionService: LogIngestionService,
    private val elasticsearchService: ElasticsearchService
) {
    val logger: LoggerImpl = LoggerImpl(LogDemoService::class.java)

    object SampleLogConstants {
        const val SAMPLE_LOG_DIR = "sample_data"
    }

    fun deleteDanglingDemoData(fileNames: List<String>, user: User) {
        fileNames.forEach{
            val version = it.split("-")[1]
            val service = it.split("-")[0]
            elasticsearchService.deleteDemoData("${user.key}_pipeline", "tags.service", service)
            elasticsearchService.deleteDemoData("${user.key}_incidents", "tags.service", service)
        }
    }

    fun createHadoopDemoForUser(user: User): List<LogReceipt> {

        val fileNames = listOf(
            "demo_hdfs_node-v1.0.0",
            "demo_hdfs_node-v1.1.0",
            "demo_node_manager-v1.0.0",
            "demo_node_manager-v1.1.0",
            "demo_resource_manager-v1.0.0",
            "demo_resource_manager-v1.1.0",
            "demo_name_node-v1.0.0",
            "demo_name_node-v1.1.0"
        )
        deleteDanglingDemoData(fileNames, user)
        val logReceipts = fileNames.map { fileName ->
            // read file
            val logMessages = this.readSampleFile(fileName)

            // process logs
            logIngestionService.processLogBatch(
                LogBatch(logs = logMessages, index = user.key)
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
