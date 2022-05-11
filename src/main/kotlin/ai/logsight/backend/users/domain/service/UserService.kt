package ai.logsight.backend.users.domain.service

import ai.logsight.backend.users.domain.OnlineUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.command.*
import ai.logsight.backend.users.domain.service.query.FindUserQuery

interface UserService {
    fun createOnlineUser(createUserCommand: CreateUserCommand): OnlineUser
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun resetPasswordWithToken(resetPasswordCommand: ResetPasswordCommand): User
    fun findUser(findUserQuery: FindUserQuery): User
    fun generateForgotPasswordTokenAndSendEmail(createTokenCommand: CreateTokenCommand)
    fun createUser(createUserCommand: CreateUserCommand): User
    fun sendActivationEmail(sendActivationEmailCommand: SendActivationEmailCommand)
    fun deleteUser(deleteUserCommand: DeleteUserCommand)
}
