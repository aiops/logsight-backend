package ai.logsight.backend.connectors.log_sink.kafka

import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.connectors.log_sink.LogSinkConnector
import ai.logsight.backend.connectors.log_sink.kafka.config.KafkaProducerConfigProperties
import org.springframework.kafka.KafkaException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.util.concurrent.ListenableFutureCallback

@Service
class KafkaProducerConnector(
    val kafkaTemplate: KafkaTemplate<String, String>,
    val kafkaProducerConfigProperties: KafkaProducerConfigProperties
) : LogSinkConnector {
    private val logger = LoggerImpl(KafkaProducerConnector::class.java)

    override fun send(msg: String): Boolean {
        val topic = kafkaProducerConfigProperties.topic
        logger.debug("Sending message $msg to kafka topic $topic.")
        val future: ListenableFuture<SendResult<String, String>> = try {
            kafkaTemplate.send(kafkaProducerConfigProperties.topic, msg)
        } catch (ex: KafkaException) {
            logger.warn("KafkaException raised while sending message $msg via topic $topic. Reason: $ex")
            return false
        }

        future.addCallback(object : ListenableFutureCallback<SendResult<String, String>> {
            override fun onSuccess(result: SendResult<String, String>?) {
                logger.debug("Successfully send message $msg to kafka topic $topic")
            }

            override fun onFailure(ex: Throwable) {
                logger.warn("Failed to send message $msg to kafka topic $topic. Reason: $ex")
            }
        })

        return true
    }
}
