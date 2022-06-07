package ai.logsight.backend.logs.demo

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.logs.domain.LogBatch
import ai.logsight.backend.logs.domain.LogsightLog
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.utils.LogFileReader
import ai.logsight.backend.users.domain.User
import logCount.LogReceipt
import org.springframework.stereotype.Service

@Service
class LogDemoService(
    private val logIngestionService: LogIngestionService
) {
    val logger: LoggerImpl = LoggerImpl(LogDemoService::class.java)

    object SampleLogConstants {
        const val SAMPLE_LOG_DIR = "sample_data"
    }

    fun createHadoopDemoForUser(user: User): List<LogReceipt> {

        val fileNames = listOf(
            "hdfs_node-v1.0.0",
            "hdfs_node-v1.1.0",
            "node_manager-v1.0.0",
            "node_manager-v1.1.0",
            "resource_manager-v1.0.0",
            "resource_manager-v1.1.0",
            "name_node-v1.0.0",
            "name_node-v1.1.0"
        )
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
