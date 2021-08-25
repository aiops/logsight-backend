package com.loxbear.logsight.models

import org.springframework.mail.SimpleMailMessage

data class Email(
    val mailFrom: String = "logsight.ai",
    val subject: String,
    val body: String
) {
    fun getSimpleMailMessage(mailTo: String): SimpleMailMessage {
        return with(SimpleMailMessage()) {
            setTo(mailTo)
            setFrom(mailFrom)
            subject?.let { setSubject(it) }
            setText(body)
            this
        }
    }
}