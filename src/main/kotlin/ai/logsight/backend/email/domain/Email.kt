package ai.logsight.backend.email.domain

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.MimeMessageHelper
import javax.mail.internet.MimeMessage

data class Email(
    val mailTo: String,
    val mailFrom: String,
    val sub: String = "Message from logsight.ai",
    val body: String
) {
    fun getMimeMessage(message: MimeMessage): MimeMessage {
        return with(MimeMessageHelper(message, true)) {
            setTo(mailTo)
            setFrom(mailFrom)
            setSubject(sub)
            setText(body, true)
            message
        }
    }

    fun getMessage(): SimpleMailMessage {
        return with(SimpleMailMessage()) {
            setTo(mailTo)
            setFrom(mailFrom)
            setSubject(sub)
            setText(body)
            this
        }
    }
}
