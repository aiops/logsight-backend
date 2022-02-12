package ai.logsight.backend.users.ports.web

import ai.logsight.backend.users.domain.service.UserService
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
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
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Api(tags = ["Users"], description = " ")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @ApiOperation("Get authenticated user")
    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    fun getUser(
        authentication: Authentication
    ): GetUserResponse {
        val user = userService.findUserByEmail(FindUserByEmailQuery(authentication.name))
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
        val user = userService.createUser(createUserCommand)
        return CreateUserResponse(id = user.id, email = user.email)
    }

    /**
     * Verify the email of the user and activate the user.
     */
    @ApiOperation("Activate registered user")
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    fun activateUser(@Valid @RequestBody activateUserRequest: ActivateUserRequest): ActivateUserResponse {

        val activateUserCommand = ActivateUserCommand(
            id = activateUserRequest.id,
            activationToken = activateUserRequest.activationToken
        )
        val activatedUser = userService.activateUser(activateUserCommand)
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
        val modifiedUser = userService.changePassword(changePasswordCommand)
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
        val modifiedUser = userService.resetPasswordWithToken(resetPasswordCommand)
        return ResetPasswordResponse(id = modifiedUser.id, email = modifiedUser.email)
    }

    /**
     * Generate a password-reset token and send it by email.
     */
    @ApiOperation("Send reset password link")
    @PostMapping("/forgot_password")
    @ResponseStatus(HttpStatus.OK)
    fun forgotPassword(@Valid @RequestBody forgotPasswordRequest: ForgotPasswordRequest) {
        userService.generateForgotPasswordTokenAndSendEmail(CreateTokenCommand(forgotPasswordRequest.email))
    }

    /**
     * Resend activation email.
     */
    @ApiOperation("Send activation email")
    @PostMapping("/resend_activation")
    @ResponseStatus(HttpStatus.OK)
    fun resendActivationEmail(@Valid @RequestBody resendActivationEmailRequest: ResendActivationEmailRequest) {
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
            println(e.message)
        }
    }
}
