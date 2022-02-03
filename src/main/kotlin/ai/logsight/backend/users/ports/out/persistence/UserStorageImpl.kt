package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.exceptions.EmailExistsException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.exceptions.UserNotFoundException
import ai.logsight.backend.users.domain.LocalUser
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toLocalUser
import ai.logsight.backend.users.extensions.toUser
import ai.logsight.backend.users.extensions.toUserEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserStorageImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserStorageService {
    override fun createUser(email: String, password: String): User {
        if (userRepository.findByEmail(email).isPresent) throw EmailExistsException("User with $email is already registered.")

        val userEntity = UserEntity(
            email = email, password = passwordEncoder.encode(password), userType = UserType.ONLINE_USER
        )
        val savedEntity = userRepository.save(userEntity)
        return savedEntity.toUser()
    }

    override fun createLocalUser(email: String, password: String): LocalUser {
        if (userRepository.findByEmail(email).isPresent) throw EmailExistsException()
        val userEntity = UserEntity(
            email = email,
            password = passwordEncoder.encode(password),
            userType = UserType.LOCAL_USER,
            activated = true,
            activationDate = LocalDateTime.now(),
            hasPaid = true
        )
        return userRepository.save(userEntity).toLocalUser()
    }

    override fun checkEmailExists(email: String): Boolean {
        return userRepository.findByEmail(email).isPresent
    }

    override fun activateUser(email: String): User {
        val userEntity = userRepository.findByEmail(email).orElseThrow { UserNotFoundException() }
        userEntity.activated = true
        return userRepository.save(userEntity).toUser()
    }

    override fun changePassword(email: String, newPassword: String, confirmNewPassword: String): User {
        val userEntity = userRepository.findByEmail(email).orElseThrow { UserNotFoundException() }

        if (newPassword != confirmNewPassword) throw PasswordsNotMatchException()

        // change password
        userEntity.password = passwordEncoder.encode(newPassword)
        // save changes
        return userRepository.save(userEntity).toUser()
    }

    override fun findUserById(userId: UUID): User =
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }.toUser()

    override fun findUserByEmail(email: String): User =
        userRepository.findByEmail(email).orElseThrow { UserNotFoundException() }.toUser()

    override fun saveUser(user: User): User {
        return userRepository.save(user.toUserEntity()).toUser()
    }
}
