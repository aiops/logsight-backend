package ai.logsight.backend.user.domain.service

import ai.logsight.backend.user.domain.LocalUser
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.domain.service.command.*

interface UserService {
    fun createUser(createUserCommand: CreateUserCommand): User
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun resetPasswordWithToken(resetPasswordCommand: ResetPasswordCommand): User
    fun findByEmail(email: String): User // fix this
    fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand)
    fun createLocalUser(createUserCommand: CreateUserCommand): LocalUser
}
