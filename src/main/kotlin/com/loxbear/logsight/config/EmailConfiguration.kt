package com.loxbear.logsight.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.thymeleaf.TemplateEngine

@Configuration
class EmailConfiguration(val emailProperties: EmailProperties) {

    @Bean
    fun getEmailSender(): JavaMailSender {
            val mailSender = JavaMailSenderImpl()
            with(emailProperties) {
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
