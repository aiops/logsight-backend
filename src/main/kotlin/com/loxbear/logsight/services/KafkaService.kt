package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationAction
import org.json.JSONObject
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>) {
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

    fun trainModels(user: LogsightUser, application: Application, baselineTagId: String, compareTagId: String) {
        val message = JSONObject()
            .put("private-key", user.key)
            .put("application_name", application.name)
            .put("status", "compare")
            .put("baselineTagId", baselineTagId)
            .put("compareTagId", compareTagId)
        val keyApplicationId = user.key + '_' + application.name
        kafkaTemplate.send("${keyApplicationId}_train", message.toString())
//        kafkaTemplate.send("${keyApplicationId}_log_compare", message.toString())
    }

    //private_key + '_application_stats'
    //quantity
    //proverka ako e nadminato
    //mail da se prati mail do userot
    // na profilot da pokazhuva momentalna sostojba
}