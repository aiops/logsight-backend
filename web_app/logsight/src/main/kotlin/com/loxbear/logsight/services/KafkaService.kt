package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import org.json.JSONObject
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>) {
    fun applicationCreated(application: Application) {
        with(application) {
            val message = JSONObject().put("private_key", user.key.toLowerCase().filter { it.isLetterOrDigit()}).put("user_name", user.email.split("@")[0]).put("application_name", name).put("application_id", id)
            kafkaTemplate.send("container_settings", message.toString())
        }
    }
}