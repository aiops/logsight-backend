package ai.logsight.backend.application.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.requests.DeleteApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.application.ports.web.responses.DeleteApplicationResponse
import ai.logsight.backend.user.ports.out.persistence.UserStorageService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/applications")
class ApplicationLifecycleController(
    private val userService: UserStorageService,
    private val applicationService: ApplicationLifecycleService
) {
    /**
     * Register a new application in the system.
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    fun createApplication(@Valid @RequestBody createApplicationRequest: CreateApplicationRequest): CreateApplicationResponse {
        val createApplicationCommand = CreateApplicationCommand(
            applicationName = createApplicationRequest.name,
            user = userService.findUserById(createApplicationRequest.userId)
        )

        val application = applicationService.createApplication(createApplicationCommand)

        return CreateApplicationResponse()
    }

    /**
     * Delete an existing application.
     */
    @GetMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    fun deleteApplication(@Valid @RequestBody deleteApplicationRequest: DeleteApplicationRequest): DeleteApplicationResponse {
        val deleteApplicationCommand = DeleteApplicationCommand(
            applicationId = deleteApplicationRequest.id,
        )

        applicationService.deleteApplication(deleteApplicationCommand)

        return DeleteApplicationResponse()
    }
}
