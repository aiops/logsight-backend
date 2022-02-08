package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.User
import java.util.*

interface UserStorageService {
    fun createUser(email: String, password: String): User
    fun findUserById(userId: UUID): User
    fun findUserByEmail(email: String): User
    fun changePassword(id: UUID, newPassword: String, confirmNewPassword: String): User
    fun activateUser(email: String): User
    fun createLocalUser(email: String, password: String): User
    fun checkEmailExists(email: String): Boolean
    fun deleteUser(id: UUID)
}
