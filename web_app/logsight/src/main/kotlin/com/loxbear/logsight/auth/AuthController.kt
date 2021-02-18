package com.loxbear.logsight.auth

import com.loxbear.logsight.charts.data.LineChart
import com.loxbear.logsight.entities.LogsightUser
import com.loxbear.logsight.models.RegisterUserForm
import com.loxbear.logsight.repositories.elasticsearch.AnomalyRepository
import com.loxbear.logsight.services.UsersService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/auth")
class AuthController(val usersService: UsersService) {

    @PostMapping("/register")
    fun register(@RequestBody form: RegisterUserForm): LogsightUser? {
        return usersService.createUser(form)
    }
}