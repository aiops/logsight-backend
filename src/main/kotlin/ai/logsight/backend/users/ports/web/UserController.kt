package ai.logsight.backend.users.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserQuery
import ai.logsight.backend.users.ports.web.request.*
import ai.logsight.backend.users.ports.web.response.ActivateUserResponse
import ai.logsight.backend.users.ports.web.response.ChangePasswordResponse
import ai.logsight.backend.users.ports.web.response.CreateUserResponse
import ai.logsight.backend.users.ports.web.response.GetUserResponse
import ai.logsight.backend.users.ports.web.response.ResetPasswordResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Api(tags = ["Users"], description = " ")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    private val logger: Logger = LoggerImpl(ApplicationLifecycleService::class.java)

    @ApiOperation("Get authenticated user")
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun getUser(@PathVariable userId: UUID): GetUserResponse {
        logger.info("Getting information for the already authenticated user.")
        val user = userService.findUser(FindUserQuery(userId))
        logger.info("User found in database. Sending response.", this::getUser.name)
        return GetUserResponse(user.id, user.email)
    }

    /**
     * Register a new user in the system.
     */
    @ApiOperation("Register new user")
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody createUserRequest: CreateUserRequest): CreateUserResponse {
        val createUserCommand = CreateUserCommand(
            email = createUserRequest.email, password = createUserRequest.password
        )
        logger.info("Starting the process for registering a new user ${createUserRequest.email}", this::createUser.name)
        val user = userService.createUser(createUserCommand)
        logger.info("New user ${createUserRequest.email} successfully created.", this::createUser.name)
        return CreateUserResponse(userId = user.id)
    }

    /**
     * Verify the email of the user and activate the user.
     */
    @ApiOperation("Activate registered user")
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    fun activateUser(@Valid @RequestBody activateUserRequest: ActivateUserRequest): ActivateUserResponse {
        logger.info("Activating user ${activateUserRequest.userId}.", this::activateUser.name)
        val activateUserCommand = ActivateUserCommand(
            id = UUID.fromString(activateUserRequest.userId), activationToken = activateUserRequest.activationToken
        )
        val activatedUser = userService.activateUser(activateUserCommand)
        logger.info("User ${activateUserRequest.userId} successfully activated.", this::activateUser.name)
        return ActivateUserResponse(userId = activatedUser.id)
    }

    /**
     * Change the user password.
     */
    @ApiOperation("Change password to existing and logged in user")
    @PostMapping("/change_password")
    @ResponseStatus(HttpStatus.OK)
    fun changePassword(
        @Valid @RequestBody changePasswordRequest: ChangePasswordRequest
    ): ChangePasswordResponse {
        val user = userService.findUser(FindUserQuery(UUID.fromString(changePasswordRequest.userId)))
        val changePasswordCommand = ChangePasswordCommand(
            email = user.email,
            oldPassword = changePasswordRequest.oldPassword,
            newPassword = changePasswordRequest.newPassword,
            confirmNewPassword = changePasswordRequest.repeatNewPassword
        )
        logger.info("Starting the process for changing password of a user ${user.id}.", this::changePassword.name)
        val modifiedUser = userService.changePassword(changePasswordCommand)
        logger.info("Password changed for a user ${user.id}.", this::changePassword.name)
        return ChangePasswordResponse(userId = modifiedUser.id)
    }

    /**
     * Receive the token from the link sent via email and display form to reset password
     */
    @ApiOperation("Reset password, when the user forgets it.")
    @PostMapping("/reset_password")
    @ResponseStatus(HttpStatus.OK)
    fun resetPassword(@Valid @RequestBody resetPasswordRequest: ResetPasswordRequest): ResetPasswordResponse {
        val resetPasswordCommand = ResetPasswordCommand(
            password = resetPasswordRequest.password,
            repeatPassword = resetPasswordRequest.repeatPassword,
            passwordResetToken = resetPasswordRequest.passwordResetToken,
            id = resetPasswordRequest.userId
        )
        logger.info(
            "Starting the process for reset password of a user ${resetPasswordRequest.userId}.", this::resetPassword.name
        )
        val modifiedUser = userService.resetPasswordWithToken(resetPasswordCommand)
        logger.info("Password reset successfully for user ${resetPasswordRequest.userId}.", this::resetPassword.name)
        return ResetPasswordResponse(userId = modifiedUser.id)
    }

    /**
     * Generate a password-reset token and send it by email.
     */
    @ApiOperation("Send reset password link")
    @PostMapping("/forgot_password")
    @ResponseStatus(HttpStatus.OK)
    fun forgotPassword(@Valid @RequestBody forgotPasswordRequest: ForgotPasswordRequest) {
        logger.info("Sending password reset link a user ${forgotPasswordRequest.email}.", this::forgotPassword.name)
        userService.generateForgotPasswordTokenAndSendEmail(CreateTokenCommand(forgotPasswordRequest.email))
    }

    /**
     * Resend activation email.
     */
    @ApiOperation("Send activation email")
    @PostMapping("/resend_activation")
    @ResponseStatus(HttpStatus.OK)
    fun resendActivationEmail(@Valid @RequestBody resendActivationEmailRequest: ResendActivationEmailRequest) {
        logger.info("Sending activation link ${resendActivationEmailRequest.email}.", this::forgotPassword.name)
        userService.sendActivationEmail(SendActivationEmailCommand(resendActivationEmailRequest.email))
    }

    @EventListener
    fun createSampleUser(event: ApplicationReadyEvent) {
        try {
            userService.createLocalUser(
                CreateUserCommand(
                    email = "clientadmin@logsight.ai", password = "samplepassword"
                )
            )
        } catch (e: Exception) {
            logger.warn("${e.message}", this::createSampleUser.name)
        }
    }
}
