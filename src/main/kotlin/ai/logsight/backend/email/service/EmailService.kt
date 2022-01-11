package ai.logsight.backend.email.service

import ai.logsight.backend.email.domain.EmailContext
import java.util.*

interface EmailService {
    fun sendActivationEmail(emailContext: EmailContext)
    fun sendPasswordResetEmail(emailContext: EmailContext)
}
