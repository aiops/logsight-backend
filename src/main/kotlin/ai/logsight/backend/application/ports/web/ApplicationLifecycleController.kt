package ai.logsight.backend.application.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.application.domain.service.command.CreateApplicationCommand
import ai.logsight.backend.application.domain.service.command.DeleteApplicationCommand
import ai.logsight.backend.application.extensions.toApplicationResponse
import ai.logsight.backend.application.ports.out.persistence.ApplicationStorageService
import ai.logsight.backend.application.ports.web.requests.CreateApplicationRequest
import ai.logsight.backend.application.ports.web.responses.CreateApplicationResponse
import ai.logsight.backend.application.ports.web.responses.DeleteApplicationResponse
import ai.logsight.backend.application.ports.web.responses.GetAllApplicationsResponse
import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.users.ports.out.persistence.UserStorageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

@Api(tags = ["Applications"], description = " ")
@RestController
@RequestMapping("/api/v1/applications")
class ApplicationLifecycleController(
    private val userService: UserStorageService,
    private val applicationService: ApplicationLifecycleService,
    private val applicationStorageService: ApplicationStorageService
) {

    private val logger: Logger = LoggerImpl(ApplicationLifecycleService::class.java)

    @ApiOperation("Get all application of the authenticated user")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getApplications(
        authentication: Authentication,
    ): GetAllApplicationsResponse {
        logger.info("Getting the authenticated user object.", this::getApplications.name)
        val user = userService.findUserByEmail(authentication.name)
        logger.info("User ${user.id} found in the database.", this::getApplications.name)
        val applications = applicationStorageService.findAllApplicationsByUser(user).map { it.toApplicationResponse() }
        logger.info("Returning back the list of applications to user ${user.id}", this::getApplications.name)
        return GetAllApplicationsResponse(
            applications = applications
        )
    }

    /**
     * Register a new application in the system.
     */
    @ApiOperation("Create application")
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun createApplication(
        authentication: Authentication,
        @Valid @RequestBody createApplicationRequest: CreateApplicationRequest
    ): CreateApplicationResponse {
        val user = userService.findUserByEmail(authentication.name)
        val createApplicationCommand = CreateApplicationCommand(
            applicationName = createApplicationRequest.applicationName,
            user = user
        )
        logger.info(
            "Creating application ${createApplicationRequest.applicationName} for user ${user.id}.",
            this::createApplication.name
        )
        val application = applicationService.createApplication(createApplicationCommand)
        logger.info(
            "Application ${application.name} with id: ${application.id} successfully created for user ${user.id}.",
            this::createApplication.name
        )
        return CreateApplicationResponse(applicationName = application.name, applicationId = application.id)
    }

    /**
     * Delete an existing application.
     */
    @ApiOperation("Delete application")
    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteApplication(
        authentication: Authentication,
        @Valid @PathVariable @Pattern(
            regexp = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",
            message = "applicationId must be UUID type."
        ) @NotEmpty(message = "applicationId must not be empty.") applicationId: UUID
    ): DeleteApplicationResponse {
        val user = userService.findUserByEmail(authentication.name)
        val deleteApplicationCommand = DeleteApplicationCommand(
            applicationId = applicationId,
            user = user
        )
        logger.info(
            "Deleting application ${deleteApplicationCommand.applicationId} for user ${user.id}.",
            this::deleteApplication.name
        )
        val application = applicationStorageService.findApplicationById(applicationId)
        applicationService.deleteApplication(deleteApplicationCommand)
        logger.info(
            "Application successfully deleted application ${deleteApplicationCommand.applicationId} for user ${user.id}.",
            this::deleteApplication.name
        )
        return DeleteApplicationResponse(applicationName = application.name, applicationId = application.id)
    }
}
