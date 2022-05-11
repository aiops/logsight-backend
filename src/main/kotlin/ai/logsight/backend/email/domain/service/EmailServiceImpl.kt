package ai.logsight.backend.email.domain.service

import ai.logsight.backend.email.domain.EmailContext
import ai.logsight.backend.email.domain.service.helpers.EmailBuilder
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailServiceImpl(
    val emailBuilder: EmailBuilder,
    val mailSender: JavaMailSender,
) : EmailService {

    override fun sendActivationEmail(emailContext: EmailContext) {
        val email = emailBuilder.buildActivationEmail(emailContext)
        // send email
        mailSender.send(email.getMimeMessage(mailSender.createMimeMessage()))
    }

    override fun sendPasswordResetEmail(emailContext: EmailContext) {
        val email = emailBuilder.buildPasswordResetEmail(emailContext)
        // send email
        mailSender.send(email.getMimeMessage(mailSender.createMimeMessage()))
    }
}
