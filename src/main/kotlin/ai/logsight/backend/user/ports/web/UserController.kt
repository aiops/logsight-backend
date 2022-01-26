package ai.logsight.backend.user.ports.web

import ai.logsight.backend.security.authentication.AuthService
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.domain.service.UserService
import ai.logsight.backend.user.domain.service.command.*
import ai.logsight.backend.user.extensions.toUserEntity
import ai.logsight.backend.user.ports.out.persistence.UserEntity
import ai.logsight.backend.user.ports.web.request.*
import ai.logsight.backend.user.ports.web.response.*
import ai.logsight.backend.user.rest.request.*
import ai.logsight.backend.user.rest.response.LoginResponse
import ai.logsight.backend.user.rest.response.UserDTO
import ai.logsight.backend.user.service.command.CreateLoginCommand
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService,
    private val authService: AuthService
) {

    @GetMapping
    fun getUser(
        authentication: Authentication
    ): User {
        val user = userService.findByEmail(authentication.name)
        return user
    }

    /**
     * Register a new user in the system.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)

    fun createUser(@Valid @RequestBody createUserRequest: CreateUserRequest): CreateUserResponse {
        val createUserCommand = CreateUserCommand(
            email = createUserRequest.email,
            password = createUserRequest.password
        )

        val user = userService.createUser(createUserCommand)

        return CreateUserResponse(id = user.id, email = user.email, privateKey = user.privateKey)
    }

    /**
     * Verify the email of the user and activate the user.
     */
    @PostMapping("/activate")
    fun activateUser(@Valid @RequestBody activateUserRequest: ActivateUserRequest): ActivateUserResponse {
        val activateUserCommand = ActivateUserCommand(
            email = activateUserRequest.email,
            activationToken = activateUserRequest.activationToken
        )
        val activatedUser = userService.activateUser(activateUserCommand)
        return ActivateUserResponse(id = activatedUser.id)
    }

    /**
     * Change the user password.
     */
    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest): ChangePasswordResponse {
        val changePasswordCommand = ChangePasswordCommand(
            email = changePasswordRequest.email,
            newPassword = changePasswordRequest.newPassword,
            confirmNewPassword = changePasswordRequest.confirmNewPassword
        )
        val modifiedUser = userService.changePassword(changePasswordCommand)
        return ChangePasswordResponse(id = modifiedUser.id)
    }

    /**
     * Generate a password-reset token and send it by email.
     */
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody resetPasswordRequest: ResetPasswordRequest): ResetPasswordResponse {
        val resetPasswordCommand = ResetPasswordCommand(
            password = resetPasswordRequest.password,
            repeatPassword = resetPasswordRequest.repeatPassword,
            passwordResetToken = resetPasswordRequest.passwordResetToken,
            email = resetPasswordRequest.email,

        )
        val modifiedUser = userService.resetPasswordWithToken(resetPasswordCommand)
        return ResetPasswordResponse(id = modifiedUser.id)
    }

    /**
     * Receive the token from the link sent via email and display form to reset password
     */
    @RequestMapping("/forgot-password", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.OK)
    fun resetUserPassword(@Valid @RequestBody forgotPasswordRequest: ForgotPasswordRequest) {
        userService.generateForgotPasswordTokenAndSendEmail(CreateTokenCommand(forgotPasswordRequest.email))
    }

    /**
     * login user (authenticate)
     */

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginUserRequest: LoginUserRequest): LoginResponse {
        val token = authService.authenticateUser(
            CreateLoginCommand(
                email = loginUserRequest.email,
                password = loginUserRequest.password
            )
        )
        val user = userService.findByEmail(loginUserRequest.email)
        return LoginResponse(token = token.token, user = UserDTO(user.id, user.email))
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
