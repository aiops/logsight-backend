package com.loxbear.logsight.services

import com.loxbear.logsight.config.EmailConfiguration
import com.loxbear.logsight.entities.LogsightUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service


@Service
class EmailService(val emailConfiguration: EmailConfiguration,
                   @Value("\${app.baseUrl}") val baseUrl: String) {

    fun sendActivationEmail(user: LogsightUser) {
        val activationUrl = "$baseUrl/auth/activate/${user.key}"
        val emailTo = user.email
        val body = "Please activate on the following <a href=$activationUrl> url </a>"
        sendEmail(emailTo, body)
    }

    fun sendEmail(emailTo: String, body: String) {
        val sender = emailConfiguration.getEmailSender()
        val message = SimpleMailMessage()
        message.setFrom("LogSight")
        message.setTo(emailTo)
        message.setSubject("Test")
        message.setText(body)
        sender.send(message)
    }

}