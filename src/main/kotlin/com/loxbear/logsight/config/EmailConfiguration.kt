package com.loxbear.logsight.config

import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class EmailConfiguration(val emailProperties: EmailProperties) {

    fun getEmailSender(): JavaMailSender =
        with(emailProperties) {
            val mailSender = JavaMailSenderImpl()
            mailSender.host = host
            mailSender.port = port
            mailSender.username = username
            mailSender.password = password

            val javaMailProperties = java.util.Properties()
            javaMailProperties.setProperty("mail.smtp.auth", "false")
            javaMailProperties.setProperty("mail.smtp.starttls.enable", "true")
            javaMailProperties.setProperty("mail.transport.protocol", "smtp")
            mailSender.javaMailProperties = javaMailProperties
            mailSender
        }
}
