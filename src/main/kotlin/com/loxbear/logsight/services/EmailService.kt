package com.loxbear.logsight.services

import com.loxbear.logsight.config.EmailConfiguration
import com.loxbear.logsight.models.auth.Email
import org.springframework.mail.MailException
import org.springframework.stereotype.Service


@Service
class EmailService(
    val emailConfiguration: EmailConfiguration,
) {

    fun sendEmail(email: Email): Email? {
        try {
            emailConfiguration
                .getEmailSender()
                .send(email.getSimpleMailMessage())
        } catch (e: MailException) {
            return null
        }
        return email
    }
}