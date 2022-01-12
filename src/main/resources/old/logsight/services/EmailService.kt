package com.loxbear.logsight.services

import com.loxbear.logsight.models.auth.Email
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service


@Service
class EmailService(
    val mailSender: JavaMailSender,
) {
    fun sendMimeEmail(email: Email): Unit = mailSender.send(
        email.getMimeMessage(
            mailSender.createMimeMessage()
        )
    )

    fun sendEmail(email: Email): Unit = mailSender.send(email.getMessage())
}