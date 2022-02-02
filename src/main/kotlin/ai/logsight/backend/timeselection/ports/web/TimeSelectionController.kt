package ai.logsight.backend.timeselection.ports.web

import ai.logsight.backend.timeselection.domain.service.TimeSelectionService
import ai.logsight.backend.timeselection.ports.web.request.PredefinedTimeRequest
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import ai.logsight.backend.users.ports.web.response.CreatePredefinedTimeSelectionResponse
import ai.logsight.backend.users.ports.web.response.CreateTimeSelectionResponse
import ai.logsight.backend.users.ports.web.response.DeleteTimeSelectionResponse
import ai.logsight.backend.users.ports.web.response.TimeSelectionResponse
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/time_ranges")
class TimeSelectionController(
    val userService: UserService,
    val timeSelectionService: TimeSelectionService
) {

    @GetMapping("")
    fun getTimeSelections(
        authentication: Authentication
    ): TimeSelectionResponse {
        return TimeSelectionResponse(
            timeSelectionService.findAllByUser(
                userService.findUserByEmail(
                    FindUserByEmailQuery(
                        authentication.name
                    )
                )
            )
        )
    }

    @PostMapping("/create")
    fun createTimeSelection(
        authentication: Authentication,
        @RequestBody request: PredefinedTimeRequest
    ): CreateTimeSelectionResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        return CreateTimeSelectionResponse(timeSelectionService.createTimeSelection(user, request))
    }

    @PostMapping("/delete")
    fun deleteTimeSelection(
        authentication: Authentication,
        @RequestBody request: PredefinedTimeRequest
    ): DeleteTimeSelectionResponse = request.id.let { id ->
        timeSelectionService.deleteTimeSelection(id)
        DeleteTimeSelectionResponse(id)
    }

    @PostMapping("/predefined")
    fun createPredefinedTimeSelections(
        authentication: Authentication,
    ): CreatePredefinedTimeSelectionResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        return CreatePredefinedTimeSelectionResponse(timeSelectionService.createPredefinedTimeSelections(user))
    }
}
