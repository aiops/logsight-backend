package ai.logsight.backend.user.rest

import ai.logsight.backend.user.rest.request.ActivateUserRequest
import ai.logsight.backend.user.rest.request.ChangePasswordRequest
import ai.logsight.backend.user.rest.request.CreateUserRequest
import ai.logsight.backend.user.rest.request.ResetPasswordRequest
import ai.logsight.backend.user.rest.response.ActivateUserResponse
import ai.logsight.backend.user.rest.response.ChangePasswordResponse
import ai.logsight.backend.user.rest.response.CreateUserResponse
import ai.logsight.backend.user.rest.response.ResetPasswordResponse
import ai.logsight.backend.user.service.UserService
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import ai.logsight.backend.user.service.command.ResetPasswordCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.* // ktlint-disable no-wildcard-imports
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {
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

    @PostMapping("/activate")
    fun activateUser(@Valid @RequestBody activateUserRequest: ActivateUserRequest): ActivateUserResponse {
        val activateUserCommand = ActivateUserCommand(
            id = activateUserRequest.id,
            email = activateUserRequest.email,
            activationToken = activateUserRequest.activationToken
        )
        val activatedUser = userService.activateUser(activateUserCommand)
        return ActivateUserResponse(id = activatedUser.id)
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest): ChangePasswordResponse {
        val changePasswordCommand = ChangePasswordCommand(
            email = changePasswordRequest.email,
            oldPassword = changePasswordRequest.oldPassword,
            newPassword = changePasswordRequest.newPassword
        )
        val modifiedUser = userService.changePassword(changePasswordCommand)
        return ChangePasswordResponse(id = modifiedUser.id)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody resetPasswordRequest: ResetPasswordRequest): ResetPasswordResponse {
        val resetPasswordCommand = ResetPasswordCommand(
            password = resetPasswordRequest.password,
            passwordResetToken = resetPasswordRequest.passwordResetToken,
            userId = resetPasswordRequest.userId
        )
        val modifiedUser = userService.resetPassword(resetPasswordCommand)
        return ResetPasswordResponse(id = modifiedUser.id)
    }
}
