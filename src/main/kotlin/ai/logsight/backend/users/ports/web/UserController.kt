package ai.logsight.backend.users.ports.web

import ai.logsight.backend.application.domain.service.ApplicationLifecycleService
import ai.logsight.backend.common.logging.Logger
import ai.logsight.backend.common.logging.LoggerImpl
import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import ai.logsight.backend.users.ports.web.request.*
import ai.logsight.backend.users.ports.web.response.*
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Users"], description = " ")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    private val logger: Logger = LoggerImpl(ApplicationLifecycleService::class.java)

    @ApiOperation("Get authenticated user")
    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    fun getUser(
        authentication: Authentication
    ): GetUserResponse {
        logger.info("Getting information for the already authenticated user.", this::getUser.name)
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
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
            email = createUserRequest.email,
            password = createUserRequest.password
        )
        logger.info("Starting the process for registering a new user ${createUserRequest.email}", this::createUser.name)
        val user = userService.createUser(createUserCommand)
        logger.info("New user ${createUserRequest.email} successfully created.", this::createUser.name)
        return CreateUserResponse(id = user.id, email = user.email)
    }

    /**
     * Verify the email of the user and activate the user.
     */
    @ApiOperation("Activate registered user")
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    fun activateUser(@Valid @RequestBody activateUserRequest: ActivateUserRequest): ActivateUserResponse {
        logger.info("Activating user ${activateUserRequest.id}.", this::activateUser.name)
        val activateUserCommand = ActivateUserCommand(
            id = activateUserRequest.id,
            activationToken = activateUserRequest.activationToken
        )
        val activatedUser = userService.activateUser(activateUserCommand)
        logger.info("User ${activateUserRequest.id} successfully activated.", this::activateUser.name)
        return ActivateUserResponse(id = activatedUser.id, email = activatedUser.email)
    }

    /**
     * Change the user password.
     */
    @ApiOperation("Change password to existing and logged in user")
    @PostMapping("/change_password")
    @ResponseStatus(HttpStatus.OK)
    fun changePassword(
        authentication: Authentication,
        @Valid @RequestBody changePasswordRequest: ChangePasswordRequest
    ): ChangePasswordResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
        val changePasswordCommand = ChangePasswordCommand(
            email = user.email,
            oldPassword = changePasswordRequest.oldPassword,
            newPassword = changePasswordRequest.newPassword,
            confirmNewPassword = changePasswordRequest.repeatNewPassword
        )
        logger.info("Starting the process for changing password of a user ${user.id}.", this::changePassword.name)
        val modifiedUser = userService.changePassword(changePasswordCommand)
        logger.info("Password changed for a user ${user.id}.", this::changePassword.name)
        return ChangePasswordResponse(id = modifiedUser.id, email = modifiedUser.email)
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
            id = resetPasswordRequest.id
        )
        logger.info(
            "Starting the process for reset password of a user ${resetPasswordRequest.id}.",
            this::resetPassword.name
        )
        val modifiedUser = userService.resetPasswordWithToken(resetPasswordCommand)
        logger.info("Password reset successfully for user ${resetPasswordRequest.id}.", this::resetPassword.name)
        return ResetPasswordResponse(id = modifiedUser.id, email = modifiedUser.email)
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
                    email = "clientadmin@logsight.ai",
                    password = "samplepassword"
                )
            )
        } catch (e: Exception) {
            logger.warn("${e.message}", this::createSampleUser.name)
        }
    }
}
