package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.users.domain.OnlineUser
import ai.logsight.backend.users.domain.User
import java.util.*

interface UserStorageService {
    fun findUserById(userId: UUID): User
    fun findUserByEmail(email: String): User
    fun createOnlineUser(email: String, password: String): OnlineUser
    fun changePassword(id: UUID, newPassword: String): User
    fun activateUser(email: String): User
    fun createUser(email: String, password: String): User
    fun checkEmailExists(email: String): Boolean
    fun deleteUser(id: UUID): User
}
