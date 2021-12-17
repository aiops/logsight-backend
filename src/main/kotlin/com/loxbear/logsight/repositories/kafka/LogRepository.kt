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

    fun logToKafka(
        userKey: String,
        authMail: String,
        appName: String,
        inputTopicName: String,
        appID: Long,
        logType: LogFileTypes,
        logs: Collection<LogMessage>
    ){
        val messagesKafka = createKafkaMessages(userKey, appName, logType.toString().toLowerCase(), logs)
        messagesKafka.forEach {
            sendToKafka(inputTopicName, jsonFormat.encodeToString(it))
        }
    }

    fun toKafka(
        authMail: String,
        appID: Long,
        logType: LogFileTypes,
        logs: Collection<LogMessage>
    ): Unit = userService.findByEmail(authMail).map { user ->
        val appName = applicationService.findById(appID).name
        val inputTopicName = applicationService.findById(appID).inputTopicName
        val messagesKafka = createKafkaMessages(user.key, appName, logType.toString().toLowerCase(), logs)
        messagesKafka.forEach { sendToKafka(inputTopicName, jsonFormat.encodeToString(it)) }
    }.orElseThrow()

    private fun createKafkaMessages (
        privateKey: String,
        appName: String,
        logType: String,
        logs: Collection<LogMessage>
    ): Collection<LogMessageKafka> {
        return logs.map { LogMessageKafka(privateKey, appName, logType, it.message) }
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
