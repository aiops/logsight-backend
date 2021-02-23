package com.loxbear.logsight.auth

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LoginUserForm
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.services.UsersService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/auth")
//TODO refactor with Spring Security
class AuthController(val usersService: UsersService) {

    @PostMapping("/register")
    fun register(@RequestBody form: RegisterUserForm): LogsightUser? {
        return usersService.createUser(form)
    }

    @PostMapping("/register/demo")
    fun registerDemo(@RequestBody body: Map<String, String>): ResponseEntity<String> {
        val result = usersService.registerUser(body["email"]!!)
        return if (result == null)
            ResponseEntity(HttpStatus.OK)
        else ResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/login")
    fun login(@RequestBody form: LoginUserForm): LogsightUser? {
        return usersService.loginUser(form)
    }
}