package ai.logsight.backend.email.domain.service.helpers

import ai.logsight.backend.common.config.CommonConfigProperties
import ai.logsight.backend.email.domain.Email
import ai.logsight.backend.email.domain.EmailContext
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.net.URL

@Service
class EmailBuilder(
    val uriResolver: URIResolver,
    val templateEngine: TemplateEngine,
    val commonConfig: CommonConfigProperties,
) {

    fun buildActivationEmail(emailContext: EmailContext): Email {
        val uri = uriResolver.getActivationURI(emailContext.token)
        return this.buildEmail(uri, emailContext)
    }

    fun buildPasswordResetEmail(emailContext: EmailContext): Email {
        val uri = uriResolver.getPasswordResetURI(emailContext.token)
        return this.buildEmail(uri, emailContext)
    }

    private fun buildEmail(uri: URL, emailContext: EmailContext): Email {
        val emailBody = templateEngine.process(
            emailContext.template.toString().lowercase(),
            with(Context()) {
                setVariable("title", emailContext.title)
                setVariable("url", uri.toString())
                setVariable("logsight_mail", commonConfig.logsightEmail)
                this
            }
        )
        // build email
        return Email(
            mailTo = emailContext.userEmail,
            mailFrom = commonConfig.logsightEmail,
            sub = emailContext.title,
            body = emailBody
        )
    }
}
