package com.loxbear.logsight.repositories.elasticsearch

import com.loxbear.logsight.models.LogMessage
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Repository
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.util.concurrent.ListenableFutureCallback
import org.springframework.kafka.core.KafkaTemplate
import java.util.logging.Logger

val log: Logger = Logger.getLogger("LogRepository")


@Repository
class LogRepository(
    @Autowired
    private val kafkaTemplate: KafkaTemplate<String, String>,

    private val userService: UserService,
    private val applicationService: ApplicationService
) {

    fun sendToKafkaLogsightJSON(
        authID: String,
        appID: String,
        logs: Collection<LogMessage>
    ){
        // val userKey = userService.findByEmail(authentication.name).key
        // val app = applicationService.findById(appID)
        print("I am storing the log messages...")
        print("Number of stored log messages: ${logs.size}")
    }

    fun sendToKafkaSyslog(
        authID: String,
        appID: String,
        logs: Collection<String>
    ){
        // val userKey = userService.findByEmail(authentication.name).key
        // val app = applicationService.findById(appID)
        print("I am storing the log messages...")
        print("Number of stored log messages: ${logs.size}")
    }

    private fun sendToKafka(topicName: String, message: String) {
        val future: ListenableFuture<SendResult<String, String>> = kafkaTemplate.send(topicName, message)

        future.addCallback(object : ListenableFutureCallback<SendResult<String, String>> {
            override fun onSuccess(result: SendResult<String, String>?) {
                log.fine("Successfully sent message to kafka topic $topicName: $message")
            }

            override fun onFailure(ex: Throwable) {
                log.warning("Failed to send message to kafka topic $topicName: $message")
            }
        })
    }
}

