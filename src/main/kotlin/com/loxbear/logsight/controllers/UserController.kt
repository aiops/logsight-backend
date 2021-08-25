package com.loxbear.logsight.controllers

import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/users")
class UserController(val userService: UserService) {

    @GetMapping
    fun getUser(authentication: Authentication): UserModel {
        val user = userService.findByEmail(authentication.name)
        with(user) {
            return UserModel(id = id, email = email, activated = activated, key = key, availableData = availableData,
                usedData = usedData, hasPaid = hasPaid)
        }
    }
}