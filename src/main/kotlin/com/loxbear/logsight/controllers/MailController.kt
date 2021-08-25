package com.loxbear.logsight.controllers

import com.loxbear.logsight.models.Email
import com.loxbear.logsight.services.EmailService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users/{userId}")
class MailController(
    val emailService: EmailService
) {
    @PutMapping("/sendMail")
    fun sendMail(@RequestBody email: Email, @PathVariable userId: Long): ResponseEntity<Any> =
        emailService.sendEmail(email, userId)

}