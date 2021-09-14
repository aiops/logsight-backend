package com.loxbear.logsight.auth

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.auth.*
import com.loxbear.logsight.repositories.UserRepository
import com.loxbear.logsight.services.AuthService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate


@RestController
@RequestMapping("/api/auth")
class AuthController(
    val userService: UserService,
    val repository: UserRepository,
    val authService: AuthService
) {

    val restTemplate: RestTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build()

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

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

    @PostMapping("/login/login-link")
    fun loginLink(@RequestBody loginLinkForm: UserLoginLinkForm): ResponseEntity<LogsightUser> {
        return when (val user = authService.sendLoginLink(loginLinkForm.email)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }
    }

    @PostMapping("/kibana/login")
    fun kibanaLogin(@RequestBody requestBody: String): ResponseEntity<String> {
        /*print("kibana/login")
        val request = UtilsService.createKibanaRequestWithHeaders(requestBody)
        val response = restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/security/v1/login", request)
        return response*/
        return ResponseEntity.ok().build()
    }

}