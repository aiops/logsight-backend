package ai.logsight.backend.user.service

import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import ai.logsight.backend.user.service.command.ResetPasswordCommand

interface UserService {
    fun createUser(createUserCommand: CreateUserCommand): User
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun resetPassword(resetPasswordCommand: ResetPasswordCommand): User
}
