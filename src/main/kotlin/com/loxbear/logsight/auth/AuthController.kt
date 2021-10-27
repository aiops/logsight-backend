package com.loxbear.logsight.auth

import com.loxbear.logsight.encoder
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.auth.*
import com.loxbear.logsight.repositories.UserRepository
import com.loxbear.logsight.services.AuthService
import com.loxbear.logsight.services.EmailService
import com.loxbear.logsight.services.UserService
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.net.URL


@RestController
@RequestMapping("/api/auth")
class AuthController(
    val userService: UserService,
    val repository: UserRepository,
    val authService: AuthService,
    val emailService: EmailService
) {

    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @Value("\${app.baseUrl}")
    private val baseUrl: String? = null

    @PostMapping("/register")
    fun register(@RequestBody registerForm: UserRegisterForm): ResponseEntity<LogsightUser> =
        when (val user = authService.registerUser(registerForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }

    @PostMapping("/activate")
    fun activateUser(@RequestBody activateForm: UserActivateForm): ResponseEntity<LogsightUser> =
        when (val user = userService.activateUser(activateForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }

    @PostMapping("/login")
    fun login(@RequestBody loginForm: UserLoginFormMail): ResponseEntity<Token> =
        when (val token = authService.loginUserMail(loginForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(token)
        }

    @PostMapping("/login_id")
    fun login(@RequestBody loginForm: UserLoginFormId): ResponseEntity<Token> =
        when (val token = authService.loginUserId(loginForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(token)
        }


    @PostMapping("/login_id_key")
    fun loginIdKey(@RequestBody loginForm: UserLoginFormId): ResponseEntity<Token> =
        when (val token = authService.loginUserId(loginForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(token)
        }

    @PostMapping("/login/login-link")
    fun loginLink(@RequestBody loginLinkForm: UserLoginLinkForm): ResponseEntity<LogsightUser> {
        var isActivated = false
        userService.findByEmail(loginLinkForm.email).map {
                user -> if(user.activated){
                isActivated = true
        }
        }
        if (!isActivated) {
            return ResponseEntity.badRequest().build()
        }
        return when (val user = authService.sendLoginLink(loginLinkForm.email)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }
    }

    @PostMapping("/kibana/login")
    fun kibanaLogin(@RequestBody requestBody: String): ResponseEntity<String> {
        val user = userService.findByKey(JSONObject(requestBody).getString("key"))
        val requestB = "{\"password\":\"${user.key}\",\"username\":\"${user.email}\"}"
        val request = UtilsService.createKibanaRequestWithHeaders(requestB)
        val response = restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/security/v1/login", request)
        return response
    }


    @PostMapping("/change_password")
    fun changePassword(@RequestBody changePasswordForm: String): ResponseEntity<LogsightUser> {
        val user = userService.findByKey(JSONObject(changePasswordForm).getString("key"))
        val oldPassword = JSONObject(changePasswordForm).getString("oldPassword")
        val newPassword = JSONObject(changePasswordForm).getString("password")
        if (encoder().matches(oldPassword,user.password)){
            userService.changePassword(UserRegisterForm(user.email, newPassword))
            return ResponseEntity.ok().body(user)
        }else{
            return ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/reset_password")
    fun resetPassword(@RequestBody emailForm: String): ResponseEntity<LogsightUser> {
        val user = userService.findByEmail(JSONObject(emailForm).getString("email")).get()
        val newPassword = utils.KeyGenerator.generate()

        if (user != null){
            userService.changePassword(UserRegisterForm(user.email, newPassword))
            emailService.sendMimeEmail(
                Email(
                    mailTo = user.email,
                    sub = "Reset password | logsight.ai",
                    body = authService.getResetPasswordMailBody(
                        "resetPasswordEmail",
                        "Reset password | logsight.ai",
                        newPassword,
                        URL(URL(baseUrl), "auth/login")
                    )
                )
            )
            return ResponseEntity.ok().body(user)
        }else{
            return ResponseEntity.badRequest().build()
        }
    }


}