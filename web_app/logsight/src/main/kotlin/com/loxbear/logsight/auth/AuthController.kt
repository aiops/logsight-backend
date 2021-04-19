package com.loxbear.logsight.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LoginUserForm
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.security.SecurityConstants
import com.loxbear.logsight.services.UsersService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/api/auth")
class AuthController(val usersService: UsersService, val authenticationManager: AuthenticationManager) {

    @PostMapping("/register")
    fun register(@RequestBody form: RegisterUserForm): LogsightUser? {
        return usersService.createUser(form)
    }

    @PostMapping("/register/demo")
    fun registerDemo(@RequestBody body: Map<String, String>): ResponseEntity<Any> {
        val result = usersService.registerUser(body["email"]!!)
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

    @PostMapping("/activate")
    fun activateUser(@RequestBody body: Map<String, String>): UserModel = usersService.activateUser(body["key"]!!)
}