package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.LocalUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.domain.service.command.ActivateUserCommand
import ai.logsight.backend.users.domain.service.command.ChangePasswordCommand
import ai.logsight.backend.users.domain.service.command.CreateUserCommand
import ai.logsight.backend.users.domain.service.query.FindUserByEmailQuery
import java.util.UUID

interface UserStorageService {
    fun createUser(email: String, password: String): User
    fun findUserById(userId: UUID): User
    fun findUserByEmail(email: String): User
    fun saveUser(user: User): User
    fun changePassword(email: String, newPassword: String, confirmNewPassword: String): User
    fun activateUser(email: String): User
    fun createLocalUser(email: String, password: String): LocalUser
}
