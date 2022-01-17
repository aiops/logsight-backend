package com.loxbear.logsight.controllers

import com.loxbear.logsight.entities.TimeSelection
import com.loxbear.logsight.models.PredefinedTimeRequest
import com.loxbear.logsight.services.TimeSelectionService
import com.loxbear.logsight.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/time_ranges")
class TimeSelectionController(
    val userService: UserService,
    val timeSelectionService: TimeSelectionService
) {

    @GetMapping("")
    fun getTimeSelections(
        authentication: Authentication
    ): List<TimeSelection> = userService.findByEmail(authentication.name).map { user ->
        timeSelectionService.findAllByUser(user)
    }.orElse(listOf())

    @PostMapping("/range")
    fun createTimeSelection(
        authentication: Authentication,
        @RequestBody request: PredefinedTimeRequest
    ): TimeSelection? = userService.findByEmail(authentication.name).map { user ->
        timeSelectionService.createTimeSelection(user, request)
    }.orElse(null)

    @PostMapping("/range/delete")
    fun deleteTimeSelection(
        authentication: Authentication,
        @RequestBody request: PredefinedTimeRequest
    ): Long = request.id.let { id ->
        timeSelectionService.deleteTimeSelection(id)
        id
    }

    @PostMapping("/predefined")
    fun createPredefinedTimeSelections(
        authentication: Authentication,
    ): List<TimeSelection> = userService.findByEmail(authentication.name).map { user ->
        timeSelectionService.createPredefinedTimeSelections(user)
    }.orElse(listOf())
}