package com.loxbear.logsight.services

import com.loxbear.logsight.config.EmailConfiguration
import com.loxbear.logsight.entities.LogsightUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*


@Service
class EmailService(
    val emailConfiguration: EmailConfiguration,
    @Value("\${app.baseUrl}") val baseUrl: String,
    val mailSender: JavaMailSender,
    val templateEngine: TemplateEngine
) {
    companion object {
        const val EMAIL_FROM: String = "LogSight"
//        const val SPRING_LOGO_IMAGE: String = "https://logsight.ai/assets/images/logsight_logo.png"
    }
    fun sendActivationEmail(user: LogsightUser) {

        val activationUrl = "$baseUrl/auth/activate/${user.key}"
        val emailTo = user.email
        val subject = "Activate your account"
        sendEmailWithTemplate(emailTo, activationUrl, subject, "activationEmail")
    }

    fun sendLoginEmail(user: LogsightUser, newLoginID: String) {
        val activationUrl = "$baseUrl/auth/activate/${newLoginID}_${user.key}"
        val emailTo = user.email
//      val body = "Please login on the following link $activationUrl"
        val subject = "EasyLogin to logsight.ai"
        sendEmailWithTemplate(emailTo, activationUrl, subject, "loginEmail")
    }
    fun sendEmailWithTemplate(emailTo: String, activationUrl: String, subject: String, templateName: String) {
        val message = mailSender.createMimeMessage()

        val helper = MimeMessageHelper(message, true)
        helper.setFrom(EMAIL_FROM)
        helper.setTo(emailTo)
        helper.setSubject(subject)

        val context = Context()
        context.setVariable("title", subject)
        context.setVariable("url", activationUrl)
        //       context.setVariable( "springLogo", SPRING_LOGO_IMAGE)

//        val clr = ClassPathResource(SPRING_LOGO_IMAGE)
//        helper.addInline("springLogo", clr)

        val content = templateEngine.process(templateName, context)
        helper.setText(content, true)

        mailSender.send(message)
    }
    // ------------------- not needed anymore
    fun sendEmail(emailTo: String, body: String, subject: String) {
//        val sender = emailConfiguration.getEmailSender()
//        not mockable
        val message = SimpleMailMessage()
        message.setFrom(EMAIL_FROM)
        message.setTo(emailTo)
        message.setSubject(subject)
        message.setText(body)
        mailSender.send(message)
    }
//---------------------------------------

    fun sendAvailableDataExceededEmail(user: LogsightUser) {
        val emailTo = user.email
        val body = "Your data has exceeded" //put a good message here
        val subject = "logsight.ai data limit exceeded"
        sendEmail(emailTo, body, subject)
    }
}