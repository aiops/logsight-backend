package ai.logsight.backend.application.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.requests.DeleteApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.application.ports.web.responses.DeleteApplicationResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/application")
class ApplicationLifecycleController(
    private val userService: UserStorageService,
    private val applicationService: ApplicationLifecycleService,
    private val applicationStorageService: ApplicationStorageService
) {
    /**
     * Register a new application in the system.
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun createApplication(authentication: Authentication, @Valid @RequestBody createApplicationRequest: CreateApplicationRequest): CreateApplicationResponse {
        val createApplicationCommand = CreateApplicationCommand(
            applicationName = createApplicationRequest.applicationName,
            user = userService.findUserByEmail(authentication.name)
        )

        val application = applicationService.createApplication(createApplicationCommand)
        return CreateApplicationResponse(description = "Application created successfully", applicationName = application.name, applicationId = application.id)
    }

    /**
     * Delete an existing application.
     */
    @DeleteMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun deleteApplication(authentication: Authentication, @Valid @RequestBody deleteApplicationRequest: DeleteApplicationRequest): DeleteApplicationResponse {
        val deleteApplicationCommand = DeleteApplicationCommand(
            applicationId = deleteApplicationRequest.applicationId,
        )
        val application = applicationStorageService.findApplicationById(deleteApplicationRequest.applicationId)
        applicationService.deleteApplication(deleteApplicationCommand)

        return DeleteApplicationResponse(description = "Application created successfully", applicationName = application.name, applicationId = application.id)
    }
}
