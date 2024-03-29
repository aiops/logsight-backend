package ai.logsight.backend.logs.ingestion.ports.out.sink.config

import ai.logsight.backend.logs.ingestion.ports.out.sink.LogSink
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.kafka.KafkaLogSinkAdapter
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.queued_zmq.BlockingQueueLogSinkAdapter
import ai.logsight.backend.logs.ingestion.ports.out.sink.adapters.zeromq.ZmqLogSinkAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LogSinkConfig(
    private val logSinkConfigProperties: LogSinkConfigProperties,
    private val zmqLogSinkAdapter: ZmqLogSinkAdapter,
    private val blockingQueueLogSinkAdapter: BlockingQueueLogSinkAdapter,
    private val kafkaLogSinkAdapter: KafkaLogSinkAdapter
) {
    @Bean
    fun logIngestionService(): LogSink =
        when (logSinkConfigProperties.type) {
            LogSinkTypes.ZMQ -> LogSink(zmqLogSinkAdapter)
            LogSinkTypes.QUEUED_ZMQ -> LogSink(blockingQueueLogSinkAdapter)
            LogSinkTypes.KAFKA -> LogSink(kafkaLogSinkAdapter)
        }
}
