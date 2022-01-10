package ai.logsight.backend.email.config

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

@Component
class ClassLoaderEmailTemplateResolver {
    @Bean
    fun emailTemplateResolver(): ClassLoaderTemplateResolver {
        val secondaryTemplateResolver = ClassLoaderTemplateResolver()
        secondaryTemplateResolver.prefix = "html-email-templates/"
        secondaryTemplateResolver.suffix = ".html"
        secondaryTemplateResolver.templateMode = TemplateMode.HTML
        secondaryTemplateResolver.characterEncoding = "UTF-8"
        secondaryTemplateResolver.order = 1
        secondaryTemplateResolver.checkExistence = true
        return secondaryTemplateResolver
    }
}
