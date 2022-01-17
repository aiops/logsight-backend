package ai.logsight.backend.user.ports.out.persistence

import ai.logsight.backend.user.domain.LocalUser
import ai.logsight.backend.user.domain.User
import ai.logsight.backend.user.domain.service.command.ActivateUserCommand
import ai.logsight.backend.user.domain.service.command.ChangePasswordCommand
import ai.logsight.backend.user.domain.service.command.CreateUserCommand
import java.util.UUID

interface UserStorageService {
    fun createUser(createUserCommand: CreateUserCommand): User
    fun createLocalUser(createUserCommand: CreateUserCommand): LocalUser
    fun activateUser(activateUserCommand: ActivateUserCommand): User
    fun changePassword(changePasswordCommand: ChangePasswordCommand): User
    fun findUserById(userId: UUID): User
    fun findUserByEmail(email: String): User
    fun saveUser(user: User): User
}
