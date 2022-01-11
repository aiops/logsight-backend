package ai.logsight.backend.email.service

import ai.logsight.backend.common.config.CommonConfigurationProperties
import ai.logsight.backend.email.domain.Email
import ai.logsight.backend.email.service.dto.ActivateUserEmailDTO
import ai.logsight.backend.email.service.dto.ResetPasswordEmailDTO
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    val commonConfig: CommonConfigurationProperties,
    val mailSender: JavaMailSender,
    val templateEngine: TemplateEngine,
) {

    fun sendActivationEmail(activateDTO: ActivateUserEmailDTO) {
        val title = "Activate your account"

        // build email body
        val emailBody = templateEngine.process(
            "email_activation",
            with(Context()) {
                setVariable("title", title)
                setVariable("url", activateDTO.activationUrl.toString())
                setVariable("logsight_mail", commonConfig.logsightEmail)
                this
            }
        )

        // build email
        val email = Email(
            mailTo = activateDTO.userEmail,
            mailFrom = commonConfig.logsightEmail,
            sub = title,
            body = emailBody
        )

        // send email
        mailSender.send(email.getMimeMessage(mailSender.createMimeMessage()))
    }

    fun sendPasswordResetEmail(resetPwDTO: ResetPasswordEmailDTO) {
        val title = "Reset password | logsight.ai"

        // build email body
        val emailBody = templateEngine.process(
            "email_reset_password",
            with(Context()) {
                setVariable("title", title)
                setVariable("url", resetPwDTO.passwordResetUrl.toString())
                setVariable("logsight_mail", commonConfig.logsightEmail)
                this
            }
        )

        // build email
        val email = Email(
            mailTo = resetPwDTO.userEmail,
            mailFrom = commonConfig.logsightEmail,
            sub = title,
            body = emailBody
        )

        // send email
        mailSender.send(email.getMimeMessage(mailSender.createMimeMessage()))
    }
}
