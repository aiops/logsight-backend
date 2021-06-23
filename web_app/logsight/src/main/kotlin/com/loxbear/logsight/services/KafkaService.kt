package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.enums.ApplicationAction
import org.json.JSONObject
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>, val usersService: UsersService) {
    fun applicationChange(application: Application, action: ApplicationAction) {
        with(application) {
            val message = JSONObject().put("private_key", user.key.toLowerCase().filter { it.isLetterOrDigit() })
                .put("user_name", user.email.split("@")[0]).put("application_name", name).put("application_id", id)
                .put("status", action.toString())
            kafkaTemplate.send("container_settings", message.toString())
        }
    }

    fun updatePayment(userKey: String, hasPaid: Boolean) {
        val message = JSONObject().put("is_active", if (hasPaid) 1 else 0)
        kafkaTemplate.send("${userKey}_subscription", message.toString())
    }

    @KafkaListener(topics = ["application_stats"], groupId = "1")
    fun applicationStatsChange(message: String) {
        val json = JSONObject(message)
        usersService.updateUsedData(json.getString("private_key"), json.getLong("quantity"))

    }

    //private_key + '_application_stats'
    //quantity
    //proverka ako e nadminato
    //mail da se prati mail do userot
    // na profilot da pokazhuva momentalna sostojba
}