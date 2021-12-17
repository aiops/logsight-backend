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
                .put("application_name", name).put("application_id", id)
                .put("status", action.toString())
            kafkaTemplate.send("manager_settings", message.toString())
        }
    }

    fun updatePayment(userKey: String, hasPaid: Boolean) {
        val message = JSONObject().put("is_active", if (hasPaid) 1 else 0)
            .put("private-key", userKey)
        kafkaTemplate.send("subscription", message.toString())
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
    }
}