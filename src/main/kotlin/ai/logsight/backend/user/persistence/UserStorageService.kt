package ai.logsight.backend.user.persistence

import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.service.command.ActivateUserCommand
import ai.logsight.backend.user.service.command.ChangePasswordCommand
import ai.logsight.backend.user.service.command.CreateUserCommand
import java.util.UUID

interface UserStorageService {
    fun createUser(createUserCommand: CreateUserCommand): User
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun getUserById(userId: UUID): User
}
