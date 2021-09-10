package com.loxbear.logsight.repositories.kafka

import com.loxbear.logsight.entities.enums.LogFileTypes
import com.loxbear.logsight.models.log.LogMessage
import com.loxbear.logsight.models.log.LogMessageKafka
import com.loxbear.logsight.services.ApplicationService
import com.loxbear.logsight.services.UserService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Repository
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.util.concurrent.ListenableFutureCallback
import org.springframework.kafka.core.KafkaTemplate
import java.util.logging.Logger

@Repository
class LogRepository(
    @Autowired
    private val kafkaTemplate: KafkaTemplate<String, String>,

    private val userService: UserService,
    private val applicationService: ApplicationService
) {

    val log: Logger = Logger.getLogger(LogRepository::class.java.toString())

    val jsonFormat = Json{}
    val topicLogstash = "logsight.logstash"

    fun toKafka(
        authMail: String,
        appID: Long,
        logType: LogFileTypes,
        logs: Collection<LogMessage>
    ){
        val privateKey = userService.findByEmail(authMail).key
        val appName = applicationService.findById(appID).name

        val topicName = "$topicLogstash.${logType.toString().toLowerCase()}"
        val messagesKafka = createKafkaMessages(privateKey, appName, logs)
        messagesKafka.forEach { sendToKafka(topicName, jsonFormat.encodeToString(it)) }
    }

    private fun createKafkaMessages (
        privateKey: String,
        appName: String,
        logs: Collection<LogMessage>
    ): Collection<LogMessageKafka> {
        return logs.map { LogMessageKafka(privateKey, appName, it) }
    }

    private fun sendToKafka(topicName: String, message: String) {
        val future: ListenableFuture<SendResult<String, String>> = kafkaTemplate.send(topicName, message)

        future.addCallback(object : ListenableFutureCallback<SendResult<String, String>> {
            override fun onSuccess(result: SendResult<String, String>?) {
                log.fine("Successfully send message to kafka topic $topicName: $message")
            }

            override fun onFailure(ex: Throwable) {
                log.warning("Failed to send message to kafka topic $topicName: $message")
            }
        })
    }
}

