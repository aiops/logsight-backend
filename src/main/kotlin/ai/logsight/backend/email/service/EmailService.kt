package ai.logsight.backend.email.service

import ai.logsight.backend.email.domain.Email
import ai.logsight.backend.token.domain.Token
import ai.logsight.backend.user.domain.User
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

    fun sendEmail(email: Email) {
        mailSender.send(email.getMessage())
    }

    fun sendActivationEmail(activationToken: Token, user: User) {
        // build Email
        // send Email this.sendEmail(email)
        TODO("IMPLEMENT PROPER EMAIL")
    }

    fun sendPasswordResetEmail(passwordResetToken: Token, user: User) {
        TODO("IMPLEMENT PROPER EMAIL")
    }



}
