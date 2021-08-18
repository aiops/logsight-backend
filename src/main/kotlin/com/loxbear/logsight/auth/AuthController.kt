package com.loxbear.logsight.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LoginUserForm
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.repositories.UserRepository
import com.loxbear.logsight.security.SecurityConstants
import com.loxbear.logsight.services.EmailService
import com.loxbear.logsight.services.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.postForEntity
import utils.UtilsService
import java.lang.RuntimeException
import java.util.*


@RestController
@RequestMapping("/api/auth")
class AuthController(val userService: UserService,
                     val authenticationManager: AuthenticationManager,
                     val emailService: EmailService,
                     val repository: UserRepository) {

    val restTemplate = RestTemplateBuilder()
        .basicAuthentication("elastic", "elasticsearchpassword")
        .build();

    @Value("\${kibana.url}")
    private val kibanaUrl: String? = null

    @PostMapping("/register")
    fun register(@RequestBody form: RegisterUserForm): LogsightUser? {
        return userService.createUser(form)
    }

    @PostMapping("/register/demo")
    fun registerDemo(@RequestBody body: Map<String, String>): ResponseEntity<Any> {
        val result = userService.registerUser(body["email"]!!)
        return if (result == null)
            ResponseEntity(HttpStatus.OK)
        else ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/login")
    fun login(@RequestBody form: LoginUserForm): Map<String, String> {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(form.email, form.password))

        SecurityContextHolder.getContext().authentication = authentication
        val token: String = JWT.create()
            .withSubject(authentication.name)
            .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))

        return mapOf("token" to token)
    }


    @PostMapping("/login/login-link")
    fun loginLink(@RequestBody form: LoginUserForm): ResponseEntity<String> {
        val user = repository.findByEmail(form.email).orElseThrow{
            RuntimeException("User not found!")
        }
        val newLoginID = userService.createLoginID(user)
        emailService.sendLoginEmail(user, newLoginID)
        return ResponseEntity(HttpStatus.OK)

    }

    @PostMapping("/kibana/login")
    fun kibanaLogin(@RequestBody requestBody: String): ResponseEntity<String> {
        val request = UtilsService.createKibanaRequestWithHeaders(requestBody)
        val response = restTemplate.postForEntity<String>("http://$kibanaUrl/kibana/api/security/v1/login", request)
        return response
    }


    @PostMapping("/activate/login-link")
    fun loginLinkUser(@RequestBody body: Map<String, String>): UserModel?{
        return userService.activateUserLoginLink(body["loginID"]!!, body["key"]!!)
    }

    @PostMapping("/activate")
    fun activateUser(@RequestBody body: Map<String, String>): UserModel = userService.activateUser(body["key"]!!)
}