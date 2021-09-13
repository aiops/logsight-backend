package com.loxbear.logsight.services

import com.loxbear.logsight.entities.Application
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.entities.enums.ApplicationAction
import com.loxbear.logsight.models.auth.Email
import com.loxbear.logsight.repositories.UserRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.net.URL


@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>,
                   val userService: UserService,
                    val paymentService: PaymentService,
                    val userRepository: UserRepository,
                    val emailService: EmailService,
                   val templateEngine: TemplateEngine) {
    val logger = LoggerFactory.getLogger(KafkaService::class.java)
    val exceededMailSubject = "Logsight.ai Limit exceeded"
    val nearlyExceededMailSubject = "Logsight.ai Limit at 80%"

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

    @KafkaListener(topics= ["application_stats"], groupId = "")
    fun consume(message:String) :Unit {
        val privateKey = JSONObject(message).getString("private_key")
        val usedDataNow = JSONObject(message).getLong("quantity")
        logger.info("Received application user stats update message: [{}]", message)
        val user = userService.findByKey(privateKey)
        val usedDataPrevious = user.usedData
        val availableData = user.availableData
        if ((usedDataPrevious + usedDataNow) > availableData){
            paymentService.updateHasPaid(user, false)
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = exceededMailSubject,
                    body = getExceededLimitsMailBody(
                        "exceededLimits",
                        exceededMailSubject)
                    )
                )
        }

        if ((usedDataPrevious + usedDataNow) > 0.8*availableData){
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = nearlyExceededMailSubject,
                    body = getExceededLimitsMailBody(
                        "nearlyExceededLimits",
                        nearlyExceededMailSubject)
                )
            )
        }

        userRepository.updateUsedData(user.key, usedDataPrevious + usedDataNow)

    }


    private fun getExceededLimitsMailBody(
        template: String,
        title: String,
    ): String = templateEngine.process(
        template,
        with(Context()) {
            setVariable("title", title)
            this
        }
    )
    //private_key + '_application_stats'
    //quantity
    //check if the quantity is over the limit
    //send email to the user that the limit is reached
    //hasPaid = false
    //update used data in the database
    // na profilot da pokazhuva momentalna sostojba
}