package com.loxbear.logsight.models.auth

import org.springframework.mail.SimpleMailMessage

data class Email(
    val mailTo: String,
    val mailFrom: String = "logsight.ai",
    val sub: String = "Message from logsight.ai",
    val body: String
) {
    fun getSimpleMailMessage(): SimpleMailMessage {
        return with(SimpleMailMessage()) {
            setTo(mailTo)
            setFrom(mailFrom)
            setSubject(sub)
            setText(body)
            this
        }
    }
}