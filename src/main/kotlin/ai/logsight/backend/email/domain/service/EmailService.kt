package ai.logsight.backend.email.domain.service

import ai.logsight.backend.email.domain.EmailContext

interface EmailService {
    fun sendActivationEmail(emailContext: EmailContext)
    fun sendPasswordResetEmail(emailContext: EmailContext)
}
