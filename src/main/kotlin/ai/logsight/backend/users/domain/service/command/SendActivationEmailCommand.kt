package ai.logsight.backend.users.domain.service.command

import ai.logsight.backend.email.domain.service.helpers.EmailTemplateTypes

class SendActivationEmailCommand(
    val email: String,
    val template: EmailTemplateTypes = EmailTemplateTypes.ACTIVATION_EMAIL
)
