package ai.logsight.backend.application.ports.web

import ai.logsight.backend.application.domain.Application
import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.application.ports.web.responses.DeleteApplicationResponse
import ai.logsight.backend.application.ports.web.responses.GetAllApplicationsResponse
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

@RestController
@RequestMapping("/api/v1/applications")
class ApplicationLifecycleController(
    private val userService: UserStorageService,
    private val applicationService: ApplicationLifecycleService,
    private val applicationStorageService: ApplicationStorageService
) {

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getApplications(
        authentication: Authentication,
    ): GetAllApplicationsResponse {
        val user = userService.findUserByEmail(authentication.name)
        return GetAllApplicationsResponse(applicationStorageService.findAllApplicationsByUser(user))
    }

    /**
     * Register a new application in the system.
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun createApplication(
        authentication: Authentication,
        @Valid @RequestBody createApplicationRequest: CreateApplicationRequest
    ): CreateApplicationResponse {
        val createApplicationCommand = CreateApplicationCommand(
            applicationName = createApplicationRequest.applicationName,
            user = userService.findUserByEmail(authentication.name)
        )
        val application = applicationService.createApplication(createApplicationCommand)
        return CreateApplicationResponse(applicationName = application.name, applicationId = application.id)
    }

    /**
     * Delete an existing application.
     */
    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteApplication(
        authentication: Authentication,
        @Valid @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "applicationId must be UUID type."
        ) @NotEmpty(message = "applicationId must not be empty.") applicationId: UUID
    ): DeleteApplicationResponse {
        val deleteApplicationCommand = DeleteApplicationCommand(
            applicationId = applicationId,
        )
        val application = applicationStorageService.findApplicationById(applicationId)
        applicationService.deleteApplication(deleteApplicationCommand)
        return DeleteApplicationResponse(applicationName = application.name, applicationId = application.id)
    }
}
