package com.loxbear.logsight.controllers

import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/users")
class UserController(val userService: UserService) {

    @GetMapping
    fun getUser(
        authentication: Authentication
    ): UserModel? = userService.findByEmail(authentication.name).map { user ->
        with(user) {
            UserModel(
                id = id,
                email = email,
                activated = activated,
                key = key,
                availableData = availableData,
                usedData = usedData,
                hasPaid = hasPaid
            )
        }
    }.orElse(null)
}