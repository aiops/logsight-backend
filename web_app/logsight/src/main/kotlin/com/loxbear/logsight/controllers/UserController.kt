package com.loxbear.logsight.controllers

import com.loxbear.logsight.models.UserModel
import com.loxbear.logsight.services.UsersService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/users")
class UserController(val usersService: UsersService) {

    @GetMapping("/{key}")
    fun getUser(@PathVariable key: String): UserModel = usersService.findByKey(key)

    @PostMapping("/activate")
    fun activateUser(@RequestBody body: Map<String, String>): UserModel = usersService.activateUser(body["key"]!!)
}