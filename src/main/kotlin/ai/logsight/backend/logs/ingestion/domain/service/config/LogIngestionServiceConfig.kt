package ai.logsight.backend.logs.ingestion.domain.service.config

import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.common.config.LogsightDeploymentType
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionService
import ai.logsight.backend.logs.ingestion.domain.service.LogIngestionServiceImpl
import ai.logsight.backend.logs.ingestion.ports.out.persistence.LogsReceiptStorageService
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queue.BlockingQueueSink
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.ZeroMqSink
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LogIngestionServiceConfig(
    val commonConfig: CommonConfigProperties,
    val applicationStorageService: ApplicationStorageService,
    val logsReceiptStorageService: LogsReceiptStorageService,
    val blockingQueueSink: BlockingQueueSink,
    val zeroMqSink: ZeroMqSink
) {
    @Bean
    fun logIngestionService(): LogIngestionService =
        when (commonConfig.deployment) {
            LogsightDeploymentType.STAND_ALONE ->
                LogIngestionServiceImpl(applicationStorageService, logsReceiptStorageService, blockingQueueSink)
            LogsightDeploymentType.WEB_SERVICE -> // TODO : This is to see if this works, change to kafka sink
                LogIngestionServiceImpl(applicationStorageService, logsReceiptStorageService, zeroMqSink)
        }
}
