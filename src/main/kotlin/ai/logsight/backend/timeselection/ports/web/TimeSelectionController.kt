package ai.logsight.backend.timeselection.ports.web

import ai.logsight.backend.timeselection.domain.service.TimeSelectionService
import ai.logsight.backend.timeselection.ports.web.request.PredefinedTimeRequest
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import ai.logsight.backend.users.ports.web.response.CreateTimeSelectionResponse
import ai.logsight.backend.users.ports.web.response.DeleteTimeSelectionResponse
import ai.logsight.backend.users.ports.web.response.TimeSelectionResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

@RestController
@RequestMapping("/api/v1/time_ranges")
class TimeSelectionController(
    val userService: UserService,
    val timeSelectionService: TimeSelectionService
) {

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
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

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTimeSelection(
        authentication: Authentication,
        @Valid @RequestBody request: PredefinedTimeRequest
    ): CreateTimeSelectionResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        return CreateTimeSelectionResponse(timeSelectionService.createTimeSelection(user, request))
    }

    @DeleteMapping("/{timeSelectionId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteTimeSelection(
        authentication: Authentication,
        @Valid @NotEmpty(message = "timeSelectionId must not be empty.") @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "timeSelectionId must be UUID type."
        ) @PathVariable timeSelectionId: UUID
    ): DeleteTimeSelectionResponse {
        timeSelectionService.deleteTimeSelection(timeSelectionId)
        return DeleteTimeSelectionResponse(timeSelectionId)
    }
}
