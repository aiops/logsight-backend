package com.loxbear.logsight.services

import com.loxbear.logsight.config.EmailConfiguration
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.Email
import com.loxbear.logsight.repositories.UserRepository
import net.bytebuddy.asm.Advice
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service


@Service
class EmailService(
    val emailConfiguration: EmailConfiguration,
    @Value("\${app.baseUrl}") val baseUrl: String,
    val userRepository: UserRepository
) {

    /*fun sendActivationEmail(user: LogsightUser) {
        val activationUrl = "$baseUrl/auth/activate/${user.key}_${user.password}"
        val emailTo = user.email
        val body = "Please activate on the following link $activationUrl"
        val subject = "Logsight.ai: Activate your account"
        sendEmail(emailTo, body, subject)
    }*/

    /*fun sendLoginEmail(user: LogsightUser, newLoginID: String) {
        val activationUrl = "$baseUrl/auth/activate/${newLoginID}_${user.key}_${user.password}"
        val emailTo = user.email
        val body = "Please login on the following link $activationUrl"
        val subject = "EasyLogin to logsight.ai"
        sendEmail(emailTo, body, subject)
    }*/

    fun sendEmail(email: Email, userId: Long): ResponseEntity<Any> {
        try {
            val user = userRepository.findById(userId).orElseThrow()
            emailConfiguration
                .getEmailSender()
                .send(email.getSimpleMailMessage(user.email))
        } catch (e: MailException) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(email)
        } catch (e: NoSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user with id $userId")
        }
        return ResponseEntity.ok().body(email)
    }

    /*fun sendAvailableDataExceededEmail(user: LogsightUser) {
        val emailTo = user.email
        val body = "Your data has exceeded" //put a good message here
        val subject = "logsight.ai data limit exceeded"
        sendEmail(emailTo, body, subject)
    }*/
}