package com.loxbear.logsight.auth

import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.LoginUserForm
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.services.UsersService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/auth")
//TODO refactor with Spring Security
class AuthController(val usersService: UsersService) {

    @PostMapping("/register")
    fun register(@RequestBody form: RegisterUserForm): LogsightUser? {
        return usersService.createUser(form)
    }

    @PostMapping("/login")
    fun login(@RequestBody form: LoginUserForm): LogsightUser? {
        return usersService.loginUser(form)
    }
}