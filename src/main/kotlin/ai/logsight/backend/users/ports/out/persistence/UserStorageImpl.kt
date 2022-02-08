package ai.logsight.backend.users.ports.out.persistence

import ai.logsight.backend.exceptions.EmailExistsException
import ai.logsight.backend.exceptions.PasswordsNotMatchException
import ai.logsight.backend.exceptions.UserNotFoundException
import ai.logsight.backend.users.domain.User
import ai.logsight.backend.users.extensions.toUser
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

    override fun createLocalUser(email: String, password: String): User {
        if (userRepository.findByEmail(email).isPresent) throw EmailExistsException("User with email $email is already registered.")
        val userEntity = UserEntity(
            email = email,
            password = passwordEncoder.encode(password),
            userType = UserType.LOCAL_USER,
            activated = true,
            activationDate = LocalDateTime.now(),
            hasPaid = true
        )
        return userRepository.save(userEntity).toUser()
    }

    override fun checkEmailExists(email: String): Boolean {
        return userRepository.findByEmail(email).isPresent
    }

    override fun activateUser(email: String): User {
        val userEntity = userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException("User with email $email doesn't exist in database.") }
        userEntity.activated = true
        return userRepository.save(userEntity).toUser()
    }

    override fun changePassword(id: UUID, newPassword: String, confirmNewPassword: String): User {
        val userEntity = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User with id $id doesn't exist in database.") }

        if (newPassword != confirmNewPassword) throw PasswordsNotMatchException("Provided passwords do not match. Please retype your password correctly.")

        // change password
        userEntity.password = passwordEncoder.encode(newPassword)
        // save changes
        return userRepository.save(userEntity).toUser()
    }

    override fun findUserById(userId: UUID): User =
        userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User with email $userId doesn't exist in database.") }.toUser()

    override fun findUserByEmail(email: String): User =
        userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException("User with email $email doesn't exist in database.") }.toUser()

    override fun deleteUser(id: UUID) {
        return userRepository.deleteById(id)
    }
}
