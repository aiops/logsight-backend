package ai.logsight.backend.user.service

import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.service.command.*
import ai.logsight.backend.user.service.command.ActivateUserCommand

interface UserService {
    fun createUser(createUserCommand: CreateUserCommand): User
    fun createLocalUser(createUserCommand: CreateUserCommand): User
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun resetPasswordWithToken(resetPasswordCommand: ResetPasswordCommand): User
    fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand)
}
