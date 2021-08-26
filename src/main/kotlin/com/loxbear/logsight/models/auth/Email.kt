package com.loxbear.logsight.models.auth

import org.springframework.mail.SimpleMailMessage

data class Email(
    val mailTo: String,
    val mailFrom: String = "logsight.ai",
    val subject: String,
    val body: String
) {
    fun getSimpleMailMessage(): SimpleMailMessage {
        return with(SimpleMailMessage()) {
            setTo(mailTo)
            setFrom(mailFrom)
            subject?.let { setSubject(it) }
            setText(body)
            this
        }
    }
}