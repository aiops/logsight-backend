package ai.logsight.backend.users.exceptions

import org.springframework.mail.MailException

class MailClientException(override val message: String? = "Mail cannot be sent. Try again.") :
    MailException(message.toString())
