package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.LocalUser
import ai.logsight.backend.users.domain.User
import java.util.*

interface UserStorageService {
    fun createUser(email: String, password: String): User
    fun findUserById(userId: UUID): User
    fun findUserByEmail(email: String): User
    fun saveUser(user: User): User
    fun changePassword(email: String, newPassword: String, confirmNewPassword: String): User
    fun activateUser(email: String): User
    fun createLocalUser(email: String, password: String): LocalUser
    fun checkEmailExists(email: String): Boolean
}
