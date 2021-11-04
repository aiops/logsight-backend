package com.loxbear.logsight.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.auth.*
import com.loxbear.logsight.security.SecurityConstants
import com.loxbear.logsight.services.elasticsearch.ElasticsearchService
import com.stripe.exception.AuthenticationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.net.URL
import java.util.*
import java.util.logging.Logger

@Service
class AuthService(
    @Value("\${app.baseUrl}") val baseUrl: String,
    val userService: UserService,
    val emailService: EmailService,
    val authenticationManager: AuthenticationManager,
    val elasticsearchService: ElasticsearchService,
    val templateEngine: TemplateEngine,
) {
    val log: Logger = Logger.getLogger(AuthService::class.java.toString())

    fun registerUser(userForm: UserRegisterForm): LogsightUser? {
        val registerMailSubject = "Activate your account"
//        val password = utils.KeyGenerator.generate()
        return userService.createUser(userForm)?.let { user ->
            emailService.sendMimeEmail(
                Email(
                    mailTo = "support@logsight.ai",
                    sub = registerMailSubject,
                    body = getResetPasswordMailBody(
                        "notifyLogsightEmail",
                        registerMailSubject,
                        "${user.email}", URL("")
                    )
                )
            )
            if (elasticsearchService.createForLogsightUser(user))
                emailService.sendMimeEmail(
                    Email(
                        mailTo = user.email,
                        sub = registerMailSubject,
                        body = getRegisterMailBody(
                            "activationEmail",
                            registerMailSubject,
                            URL(URL(baseUrl), "auth/activate/${user.id}/${user.key}")
                        )
                    )
                ).let { user }
            else
                null
        }
    }

    fun sendLoginLink(email: String): LogsightUser? {
        val loginMailSubject = "EasyLogin to logsight.ai"
        val newPassword = utils.KeyGenerator.generate()
        return userService.changePassword(UserRegisterForm(email = email, password = newPassword))?.let { user ->
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = loginMailSubject,
                    body = getRegisterMailBody(
                        "loginEmail",
                        loginMailSubject,
                        URL(URL(baseUrl), "auth/login/${user.id}/$newPassword")
                    )
                )
            ).let { user }
        }
    }

    fun loginUserMail(loginForm: UserLoginFormMail): Token? =
        userService.findByEmail(loginForm.email).map { user ->
            loginUser(user.email, loginForm.password)
        }.orElse(null)

    fun loginUserId(loginForm: UserLoginFormId): Token? =
        userService.findById(loginForm.id).map { user ->
            loginUser(user.email, loginForm.password)
        }.orElse(null)

    private fun loginUser(email: String, password: String): Token? =
        try {
            with(
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(email, password)
                )
            ) {
                SecurityContextHolder.getContext().authentication = this
                Token(
                    JWT.create()
                        .withSubject(this.name)
                        .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                        .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
                )
            }
        } catch (e: AuthenticationException) {
            log.warning(e.toString())
            null
        }

    fun getRegisterMailBody(
        template: String,
        title: String,
        url: URL
    ): String = templateEngine.process(
        template,
        with(Context()) {
            setVariable("title", title)
            setVariable("url", url.toString())
            this
        }
    )


    fun getResetPasswordMailBody(
        template: String,
        title: String,
        password: String,
        url: URL
    ): String = templateEngine.process(
        template,
        with(Context()) {
            setVariable("title", title)
            setVariable("url", url.toString())
            setVariable("password", password)
            this
        }
    )

    fun getRegisterTryBody(
        template: String,
        title: String,
        password: String,
        url: URL
    ): String = templateEngine.process(
        template,
        with(Context()) {
            setVariable("title", title)
            setVariable("url", url.toString())
            setVariable("password", password)
            this
        }
    )
}