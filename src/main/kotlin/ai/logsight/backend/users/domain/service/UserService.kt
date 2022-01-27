package ai.logsight.backend.users.domain.service

import ai.logsight.backend.users.domain.LocalUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery

interface UserService {
    fun createUser(createUserCommand: CreateUserCommand): User
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun resetPasswordWithToken(resetPasswordCommand: ResetPasswordCommand): User
    fun findUserByEmail(findUserByEmailQuery: FindUserByEmailQuery): User
    fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand)
    fun createLocalUser(createUserCommand: CreateUserCommand): LocalUser
    fun sendActivationEmail(sendActivationEmailCommand: SendActivationEmailCommand)
}
