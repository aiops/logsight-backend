package com.loxbear.logsight.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.auth.Email
import com.loxbear.logsight.models.auth.Token
import com.loxbear.logsight.models.auth.UserLoginForm
import com.loxbear.logsight.models.auth.UserRegisterForm
import com.loxbear.logsight.security.SecurityConstants
import com.stripe.exception.AuthenticationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    @Value("\${app.baseUrl}") val baseUrl: String,
    val userService: UserService,
    val emailService: EmailService,
    val authenticationManager: AuthenticationManager,
) {

    fun registerUser(userForm: UserRegisterForm): LogsightUser? {
        val password = utils.KeyGenerator.generate()
        return userService.createUser(userForm.copy(password=password))?.let { user ->
            emailService.sendEmail(
                Email(
                    mailTo = user.email,
                    subject = "Logsight.ai: Activate your account",
                    body = "Please activate on the following link " +
                            "$baseUrl/auth/activate/${user.id}/${user.key}/$password"
                )
            )?.let { user }
        }
    }

    fun loginUser(loginForm: UserLoginForm): Token? =
        userService.getUser(loginForm.id)?.let { user ->
            try {
                with(authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(user.email, loginForm.password)
                )) {
                    SecurityContextHolder.getContext().authentication = this
                    Token(
                        JWT.create()
                            .withSubject(this.name)
                            .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                            .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
                    )
                }
            } catch(e: AuthenticationException) {
                null
            }
        }

    fun sendLoginLink(email: String): LogsightUser? {
        val newPassword = utils.KeyGenerator.generate()
        return userService.changePassword(UserRegisterForm(email = email, password = newPassword))?.let { user ->
            emailService.sendEmail(
                Email(
                    mailTo = user.email,
                    subject = "Logsight.ai: Login link",
                    body = "You can login via " +
                            "$baseUrl/auth/login/${user.id}/$newPassword"
                )
            ).let { user }
        }
    }
}