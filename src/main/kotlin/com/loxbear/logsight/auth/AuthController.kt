package com.loxbear.logsight.auth

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.auth.Token
import com.loxbear.logsight.models.auth.UserActivateForm
import com.loxbear.logsight.models.auth.UserLoginForm
import com.loxbear.logsight.models.auth.UserRegisterForm
import com.loxbear.logsight.repositories.UserRepository
import com.loxbear.logsight.services.AuthService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import utils.UtilsService


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
        when(val user = authService.registerUser(registerForm)){
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }

    @PutMapping("/activate")
    fun activateUser(@RequestBody activateForm: UserActivateForm): ResponseEntity<LogsightUser> =
        when(val user = userService.activateUser(activateForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }

    @PostMapping("/login")
    fun login(@RequestBody loginForm: UserLoginForm): ResponseEntity<Token>  =
        when(val token = authService.loginUser(loginForm)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(token)
        }

    @GetMapping("/login/login-link")
    fun loginLink(@RequestBody email: String): ResponseEntity<LogsightUser> =
        when(val user = authService.sendLoginLink(email)) {
            null -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.ok().body(user)
        }

    @PostMapping("/kibana/login")
    fun kibanaLogin(@RequestBody requestBody: String): ResponseEntity<String> {
        print("kibana/login")
        val request = UtilsService.createKibanaRequestWithHeaders(requestBody)
        val response = restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/security/v1/login", request)
        return response
    }

}