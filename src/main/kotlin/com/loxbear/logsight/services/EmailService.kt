package com.loxbear.logsight.services

import com.loxbear.logsight.config.EmailConfiguration
import com.loxbear.logsight.entities.LogsightUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service
import java.util.*


@Service
class EmailService(
    val emailConfiguration: EmailConfiguration,
    @Value("\${app.baseUrl}") val baseUrl: String
) {

    fun sendActivationEmail(user: LogsightUser) {
        val activationUrl = "$baseUrl/auth/activate/${user.key}"
        val emailTo = user.email
        val body = "Please activate on the following link $activationUrl"
        println(activationUrl)
        val subject = "Activate your account"
        sendEmail(emailTo, body, subject)
    }

    fun sendLoginEmail(user: LogsightUser, newLoginID: String) {
        val activationUrl = "$baseUrl/auth/activate/${newLoginID}" + "_" + user.key
        println(activationUrl)
        val emailTo = user.email
        val body = "Please login on the following link $activationUrl"
        val subject = "EasyLogin to logsight.ai"
        sendEmail(emailTo, body, subject)
    }

    fun sendEmail(emailTo: String, body: String, subject: String) {
        val sender = emailConfiguration.getEmailSender()
        val message = SimpleMailMessage()
        message.setFrom("LogSight")
        message.setTo(emailTo)
        message.setSubject(subject)
        message.setText(body)
        sender.send(message)
    }

    fun sendAvailableDataExceededEmail(user: LogsightUser) {
        val emailTo = user.email
        val body = "Your data has exceeded" //put a good message here
        val subject = "logsight.ai data limit exceeded"
        sendEmail(emailTo, body, subject)
    }

}