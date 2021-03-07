package com.loxbear.logsight.controllers

import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.services.UsersService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/users")
class UserController(val usersService: UsersService) {

    @GetMapping
    fun getUser(authentication: Authentication): UserModel {
        val user = usersService.findByEmail(authentication.name)
        with(user) {
            return UserModel(id = id, email = email, activated = activated, key = key)
        }
    }

    @PostMapping("/activate")
    fun activateUser(@RequestBody body: Map<String, String>): UserModel = usersService.activateUser(body["key"]!!)
}